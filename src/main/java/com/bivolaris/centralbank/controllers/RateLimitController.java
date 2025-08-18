package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.config.RateLimitProperties;
import com.bivolaris.centralbank.ratelimit.RateLimitStore;
import com.bivolaris.centralbank.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/rate-limit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RateLimitController {

    private final RateLimitStore rateLimitStore;
    private final RateLimitProperties rateLimitProperties;
    private final AuditService auditService;

   
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "RATE_LIMIT_STATUS_ACCESSED");
        }

        Map<String, Object> status = new HashMap<>();
        status.put("enabled", rateLimitProperties.getGlobal().isEnabled());
        status.put("storeSize", rateLimitStore.getStoreSize());
        status.put("configuration", buildConfigurationMap());

        return ResponseEntity.ok(status);
    }

    
    @GetMapping("/clear")
    public ResponseEntity<String> clearRateLimitStore() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "RATE_LIMIT_STORE_CLEARED");
        }

        rateLimitStore.clear();
        return ResponseEntity.ok("Rate limit store cleared");
    }

    
    private Map<String, Object> buildConfigurationMap() {
        Map<String, Object> config = new HashMap<>();
        
        Map<String, Object> global = new HashMap<>();
        global.put("requestsPerMinute", rateLimitProperties.getGlobal().getRequestsPerMinute());
        global.put("requestsPerHour", rateLimitProperties.getGlobal().getRequestsPerHour());
        global.put("skipInternalRequests", rateLimitProperties.getGlobal().isSkipInternalRequests());
        config.put("global", global);

        Map<String, Object> auth = new HashMap<>();
        auth.put("loginAttemptsPerMinute", rateLimitProperties.getAuth().getLoginAttemptsPerMinute());
        auth.put("loginAttemptsPerHour", rateLimitProperties.getAuth().getLoginAttemptsPerHour());
        auth.put("registrationAttemptsPerHour", rateLimitProperties.getAuth().getRegistrationAttemptsPerHour());
        config.put("auth", auth);

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("transactionsPerMinute", rateLimitProperties.getTransaction().getTransactionsPerMinute());
        transaction.put("transactionsPerHour", rateLimitProperties.getTransaction().getTransactionsPerHour());
        config.put("transaction", transaction);

        Map<String, Object> account = new HashMap<>();
        account.put("accountCreationPerHour", rateLimitProperties.getAccount().getAccountCreationPerHour());
        account.put("accountLookupPerMinute", rateLimitProperties.getAccount().getAccountLookupPerMinute());
        config.put("account", account);

        return config;
    }
}
