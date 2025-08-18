package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.entities.Auditlog;
import com.bivolaris.centralbank.entities.Bank;
import com.bivolaris.centralbank.entities.Employee;
import com.bivolaris.centralbank.repositories.AuditLogRepository;
import com.bivolaris.centralbank.repositories.AuthRepository;
import com.bivolaris.centralbank.repositories.BankRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuthRepository authRepository;
    private final BankRepository bankRepository;
    
    // Pattern to detect potentially malicious content
    private static final Pattern MALICIOUS_PATTERN = Pattern.compile(
        "(?i)(script|javascript|vbscript|onload|onerror|onclick|eval|expression|import|require|<|>|&lt;|&gt;|\\||;|\\n|\\r|\\t)"
    );

    @Transactional
    public void logEmployeeAction(Long authId, String action) {
        try {
            var auth = authRepository.findByIdWithEmployee(authId).orElse(null);
            if (auth == null || auth.getEmployee() == null) {
                log.warn("Cannot log audit action - employee not found for authId: {}", authId);
                return;
            }

            String ipAddress = getClientIpAddress();
            Employee employee = auth.getEmployee();

            Bank contextBank = getCurrentBankContext();
            
            Auditlog auditlog = new Auditlog(employee, contextBank, action, ipAddress);
            
            auditLogRepository.save(auditlog);
            log.info("Audit log created for employee: {} action: {}", employee.getEmail(), action);
        } catch (Exception e) {
            log.error("Failed to create audit log for authId: {} action: {}", authId, action, e);
        }
    }


    @Transactional
    public void logBankAction(Bank bank, String action) {
        try {
            String ipAddress = getClientIpAddress();
            Auditlog auditlog = new Auditlog(bank, action, ipAddress);
            
            auditLogRepository.save(auditlog);
            log.info("Audit log created for bank: {} action: {}", bank.getName(), action);
        } catch (Exception e) {
            log.error("Failed to create audit log for bank: {} action: {}", bank.getName(), action, e);
        }
    }


    @Transactional
    public void logEmployeeBankAction(Long authId, Bank bank, String action) {
        try {
            var auth = authRepository.findByIdWithEmployee(authId).orElse(null);
            if (auth == null || auth.getEmployee() == null) {
                log.warn("Cannot log audit action - employee not found for authId: {}", authId);
                return;
            }

            String ipAddress = getClientIpAddress();
            Employee employee = auth.getEmployee();
            
            Auditlog auditlog = new Auditlog(employee, bank, action, ipAddress);
            
            auditLogRepository.save(auditlog);
            log.info("Audit log created for employee: {} bank: {} action: {}", 
                    employee.getEmail(), bank.getName(), action);
        } catch (Exception e) {
            log.error("Failed to create audit log for authId: {} bank: {} action: {}", 
                    authId, bank.getName(), action, e);
        }
    }


    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            
            return getClientIpAddress(request);
        } catch (Exception e) {
            log.warn("Could not extract IP address from request context", e);
            return "unknown";
        }
    }


    public String getClientIpAddress(HttpServletRequest request) {
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


    private Bank getCurrentBankContext() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return null;
            }


            if (authentication.getPrincipal() instanceof String bankIdStr) {
                try {
                    UUID bankId = UUID.fromString(bankIdStr);
                    return bankRepository.findById(bankId).orElse(null);
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID, continue
                }
            }

            try {
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = attributes.getRequest();
                
                String bankCode = request.getHeader("X-Bank-Code");
                if (bankCode != null && !bankCode.isEmpty()) {
                    return bankRepository.findBySwift(bankCode).orElse(null);
                }
            } catch (Exception e) {
                // Request context not available
            }

            return null;
        } catch (Exception e) {
            log.warn("Could not determine bank context", e);
            return null;
        }
    }


    private String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "[EMPTY]";
        }
        

        if (input.length() > 100) {
            input = input.substring(0, 97) + "...";
        }
        

        String sanitized = input
            .replaceAll("[\\r\\n\\t]", " ")
            .replaceAll("[<>\"'&]", "_")    
            .replaceAll("\\|", "_")          
            .replaceAll(";", "_")            
            .trim();
        
        // Check for malicious patterns
        if (MALICIOUS_PATTERN.matcher(sanitized).find()) {
            return "[SANITIZED_INPUT]";
        }
        
        return sanitized;
    }

 
    public void logTransactionAction(Long authId, String action, String fromAccount, String toAccount, String amount) {
        String sanitizedAction = sanitizeInput(action);
        String sanitizedFromAccount = sanitizeInput(fromAccount);
        String sanitizedToAccount = sanitizeInput(toAccount);
        String sanitizedAmount = sanitizeInput(amount);
        
        String safeMessage = sanitizedAction + ": " + sanitizedFromAccount + " -> " + 
                           sanitizedToAccount + " Amount: " + sanitizedAmount;
        
        logEmployeeAction(authId, safeMessage);
    }

    public void logAccountAction(Long authId, String action, String accountInfo, String customerInfo) {
        String sanitizedAction = sanitizeInput(action);
        String sanitizedAccountInfo = sanitizeInput(accountInfo);
        String sanitizedCustomerInfo = sanitizeInput(customerInfo);
        
        String safeMessage = sanitizedAction + ": " + sanitizedAccountInfo + 
                           (customerInfo != null ? " for " + sanitizedCustomerInfo : "");
        
        logEmployeeAction(authId, safeMessage);
    }


    public void logSafeAction(Long authId, String action, String... parameters) {
        String sanitizedAction = sanitizeInput(action);
        StringBuilder safeMessage = new StringBuilder(sanitizedAction);
        
        if (parameters != null && parameters.length > 0) {
            safeMessage.append(":");
            for (String param : parameters) {
                safeMessage.append(" ").append(sanitizeInput(param));
            }
        }
        
        logEmployeeAction(authId, safeMessage.toString());
    }

    public void logSecureTransactionAction(Long authId, String action, String requestHash) {
        String safeMessage = action;
        if (requestHash != null && !requestHash.trim().isEmpty()) {
            safeMessage += " [Ref: " + requestHash.substring(0, Math.min(8, requestHash.length())) + "]";
        }
        logEmployeeAction(authId, safeMessage);
    }
}
