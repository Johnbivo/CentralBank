package com.bivolaris.centralbank.filters;

import com.bivolaris.centralbank.config.RateLimitProperties;
import com.bivolaris.centralbank.ratelimit.RateLimitResult;
import com.bivolaris.centralbank.ratelimit.RateLimitStore;
import com.bivolaris.centralbank.services.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitStore rateLimitStore;
    private final RateLimitProperties rateLimitProperties;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        if (!rateLimitProperties.getGlobal().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (rateLimitProperties.getGlobal().isSkipInternalRequests() && isInternalRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIdentifier = getClientIdentifier(request);
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        RateLimitResult endpointResult = checkEndpointRateLimit(clientIdentifier, requestPath, method);
        if (endpointResult != null && endpointResult.isRateLimitExceeded()) {
            handleRateLimitExceeded(request, response, endpointResult, "Endpoint rate limit exceeded");
            return;
        }


        RateLimitResult globalResult = checkGlobalRateLimit(clientIdentifier);
        if (globalResult.isRateLimitExceeded()) {
            handleRateLimitExceeded(request, response, globalResult, "Global rate limit exceeded");
            return;
        }

        addRateLimitHeaders(response, globalResult, endpointResult);

        filterChain.doFilter(request, response);
    }


    private RateLimitResult checkEndpointRateLimit(String clientId, String path, String method) {
        if (path.startsWith("/api/auth/")) {
            return checkAuthEndpointLimits(clientId, path, method);
        } else if (path.startsWith("/api/transactions/")) {
            return checkTransactionLimits(clientId);
        } else if (path.startsWith("/api/accounts/")) {
            return checkAccountLimits(clientId, method);
        } else if (path.startsWith("/api/fraud/")) {
            return checkFraudLimits(clientId, method);
        } else if (path.startsWith("/api/admin/") || path.startsWith("/api/audit/")) {
            return checkAdminLimits(clientId);
        }
        return null;
    }



    private RateLimitResult checkAuthEndpointLimits(String clientId, String path, String method) {
        if (path.contains("/login") && "POST".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":auth:login", 
                rateLimitProperties.getAuth().getLoginAttemptsPerMinute(), 
                60
            );
        } else if (path.contains("/register") && "POST".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":auth:register", 
                rateLimitProperties.getAuth().getRegistrationAttemptsPerHour(), 
                3600
            );
        } else if (path.contains("/refresh") && "POST".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":auth:refresh", 
                rateLimitProperties.getAuth().getTokenRefreshPerMinute(), 
                60
            );
        } else if (path.contains("/bank-token") && "POST".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":auth:bank-token", 
                rateLimitProperties.getAuth().getBankTokenRequestsPerMinute(), 
                60
            );
        }
        return null;
    }


    private RateLimitResult checkTransactionLimits(String clientId) {
        return rateLimitStore.checkAndIncrement(
            clientId + ":transactions", 
            rateLimitProperties.getTransaction().getTransactionsPerMinute(), 
            60
        );
    }

    private RateLimitResult checkAccountLimits(String clientId, String method) {
        if ("POST".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":accounts:create", 
                rateLimitProperties.getAccount().getAccountCreationPerHour(), 
                3600
            );
        } else if ("GET".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":accounts:lookup", 
                rateLimitProperties.getAccount().getAccountLookupPerMinute(), 
                60
            );
        }
        return null;
    }


    private RateLimitResult checkFraudLimits(String clientId, String method) {
        if ("POST".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":fraud:review", 
                rateLimitProperties.getFraud().getFraudReviewsPerMinute(), 
                60
            );
        } else if ("GET".equals(method)) {
            return rateLimitStore.checkAndIncrement(
                clientId + ":fraud:query", 
                rateLimitProperties.getFraud().getFraudQueryPerMinute(), 
                60
            );
        }
        return null;
    }


    private RateLimitResult checkAdminLimits(String clientId) {
        return rateLimitStore.checkAndIncrement(
            clientId + ":admin", 
            rateLimitProperties.getAdmin().getAdminActionsPerMinute(), 
            60
        );
    }


    private RateLimitResult checkGlobalRateLimit(String clientId) {
        return rateLimitStore.checkAndIncrement(
            clientId + ":global", 
            rateLimitProperties.getGlobal().getRequestsPerMinute(), 
            60
        );
    }


    private void handleRateLimitExceeded(HttpServletRequest request, HttpServletResponse response, 
                                       RateLimitResult result, String reason) throws IOException {
        

        logRateLimitViolation(request, reason);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        

        addRateLimitHeaders(response, result, null);
        

        response.setHeader("Retry-After", String.valueOf(result.getRetryAfterSeconds()));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Rate limit exceeded");
        errorResponse.put("message", reason);
        errorResponse.put("remainingRequests", result.getRemainingRequests());
        errorResponse.put("resetTime", result.getResetTime());
        errorResponse.put("retryAfter", result.getRetryAfterSeconds());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }


    private void addRateLimitHeaders(HttpServletResponse response, RateLimitResult globalResult, RateLimitResult endpointResult) {
        if (globalResult != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(globalResult.getMaxRequests()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(globalResult.getRemainingRequests()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(globalResult.getResetTime()));
        }
        
        if (endpointResult != null) {
            response.setHeader("X-RateLimit-Endpoint-Limit", String.valueOf(endpointResult.getMaxRequests()));
            response.setHeader("X-RateLimit-Endpoint-Remaining", String.valueOf(endpointResult.getRemainingRequests()));
            response.setHeader("X-RateLimit-Endpoint-Reset", String.valueOf(endpointResult.getResetTime()));
        }
    }


    private void logRateLimitViolation(HttpServletRequest request, String reason) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
                auditService.logEmployeeAction(authId, "RATE_LIMIT_EXCEEDED");
            } else {

                String clientId = getClientIdentifier(request);
                log.warn("Rate limit exceeded for client: {} on path: {} - {}", 
                    clientId, request.getRequestURI(), reason);
            }
        } catch (Exception e) {
            log.error("Failed to log rate limit violation", e);
        }
    }

    private String getClientIdentifier(HttpServletRequest request) {

        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
                return "user:" + authId;
            }
        } catch (Exception e) {
            // Fall back to IP-based identification
        }

        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp;
    }


    private String getClientIpAddress(HttpServletRequest request) {
        String[] ipHeaderCandidates = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : ipHeaderCandidates) {
            String ipAddress = request.getHeader(header);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                if (ipAddress.contains(",")) {
                    ipAddress = ipAddress.split(",")[0].trim();
                }
                return ipAddress;
            }
        }

        return request.getRemoteAddr();
    }


    private boolean isInternalRequest(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        

        return clientIp.equals("127.0.0.1") || 
               clientIp.equals("::1") || 
               clientIp.startsWith("192.168.") || 
               clientIp.startsWith("10.") ||
               clientIp.startsWith("172.");
    }
}
