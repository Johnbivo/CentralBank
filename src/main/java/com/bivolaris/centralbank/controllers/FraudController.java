package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.dtos.FraudCaseDto;
import com.bivolaris.centralbank.dtos.FraudReviewRequest;
import com.bivolaris.centralbank.entities.Employee;
import com.bivolaris.centralbank.entities.Fraudcase;
import com.bivolaris.centralbank.entities.FraudStatus;
import com.bivolaris.centralbank.mappers.FraudCaseMapper;
import com.bivolaris.centralbank.services.AuditService;
import com.bivolaris.centralbank.services.EmployeeService;
import com.bivolaris.centralbank.services.FraudDetectionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/fraud")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final FraudCaseMapper fraudCaseMapper;
    private final EmployeeService employeeService;
    private final AuditService auditService;


    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<List<FraudCaseDto>> getPendingFraudCases(Authentication authentication) {

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "FRAUD_PENDING_CASES_ACCESSED");
        }
        
        List<Fraudcase> pendingCases = fraudDetectionService.getPendingFraudCases();
        List<FraudCaseDto> caseDtos = pendingCases.stream()
                .map(fraudCaseMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(caseDtos);
    }


    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<List<FraudCaseDto>> getFraudCasesByStatus(@PathVariable FraudStatus status, Authentication authentication) {

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logSafeAction(authId, "FRAUD_CASES_BY_STATUS_ACCESSED", status != null ? status.toString() : "null");
        }
        
        List<Fraudcase> cases = fraudDetectionService.getFraudCasesByStatus(status);
        List<FraudCaseDto> caseDtos = cases.stream()
                .map(fraudCaseMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(caseDtos);
    }


    @PostMapping("/review")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<?> reviewFraudCase(
            @RequestBody FraudReviewRequest request,
            Authentication authentication) {
        
        try {
            String employeeEmail = authentication.getName();
            Employee reviewer = employeeService.findByEmail(employeeEmail);
            
            if (reviewer == null) {
                return ResponseEntity.badRequest().body("Employee not found");
            }

            if (request.getDecision() != FraudStatus.REVIEWED && 
                request.getDecision() != FraudStatus.DISMISSED) {
                return ResponseEntity.badRequest().body("Invalid decision. Must be REVIEWED or DISMISSED");
            }

            boolean success = fraudDetectionService.reviewFraudCase(
                    request.getFraudCaseId(),
                    request.getDecision(),
                    reviewer
            );

            if (success) {

                if (authentication.getPrincipal() instanceof Long authId) {
                    auditService.logSafeAction(authId, "FRAUD_CASE_REVIEWED", 
                        request.getFraudCaseId() != null ? request.getFraudCaseId() : "null",
                        request.getDecision() != null ? request.getDecision().toString() : "null");
                }
                return ResponseEntity.ok().body("Fraud case reviewed successfully");
            } else {
                if (authentication.getPrincipal() instanceof Long authId) {
                    auditService.logSafeAction(authId, "FRAUD_CASE_REVIEW_FAILED", 
                        request.getFraudCaseId() != null ? request.getFraudCaseId() : "null");
                }
                return ResponseEntity.badRequest().body("Failed to review fraud case");
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error reviewing fraud case: " + e.getMessage());
        }
    }


    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<FraudCaseDto>> getAllFraudCases(Authentication authentication) {

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "ALL_FRAUD_CASES_ACCESSED");
        }
        
        List<Fraudcase> allCases = fraudDetectionService.getFraudCasesByStatus(FraudStatus.PENDING);
        allCases.addAll(fraudDetectionService.getFraudCasesByStatus(FraudStatus.REVIEWED));
        allCases.addAll(fraudDetectionService.getFraudCasesByStatus(FraudStatus.DISMISSED));
        
        List<FraudCaseDto> caseDtos = allCases.stream()
                .map(fraudCaseMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(caseDtos);
    }
}
