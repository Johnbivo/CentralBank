package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.entities.Auditlog;
import com.bivolaris.centralbank.entities.AuditStatus;
import com.bivolaris.centralbank.repositories.AuditLogRepository;
import com.bivolaris.centralbank.services.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;


    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Auditlog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Auditlog> auditLogs;

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "AUDIT_LOGS_ACCESSED");
        }

        if (status != null && !status.isEmpty()) {
            try {
                AuditStatus auditStatus = AuditStatus.valueOf(status.toUpperCase());
                auditLogs = auditLogRepository.findByStatusOrderByActionAtDesc(auditStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else if (action != null && !action.isEmpty()) {
            auditLogs = auditLogRepository.findByActionContainingIgnoreCaseOrderByActionAtDesc(action, pageable);
        } else if (startDate != null && endDate != null) {
            try {
                LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                auditLogs = auditLogRepository.findByActionAtBetweenOrderByActionAtDesc(start, end, pageable);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            auditLogs = auditLogRepository.findAll(pageable);
        }

        return ResponseEntity.ok(auditLogs);
    }


    @GetMapping("/unreviewed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Auditlog>> getUnreviewedLogs(
            @RequestParam(defaultValue = "50") int limit) {


        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "UNREVIEWED_AUDIT_LOGS_ACCESSED");
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<Auditlog> unreviewed = auditLogRepository.findRecentUnreviewedLogs(pageable);
        
        return ResponseEntity.ok(unreviewed);
    }

    @PutMapping("/logs/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsReviewed(@PathVariable UUID id) {
        Auditlog auditLog = auditLogRepository.findById(id).orElse(null);
        if (auditLog == null) {
            return ResponseEntity.notFound().build();
        }

        auditLog.setStatus(AuditStatus.REVIEWED);
        auditLogRepository.save(auditLog);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "AUDIT_LOG_REVIEWED: " + id);
        }

        return ResponseEntity.ok().build();
    }


    @PostMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createAuditLog(
            @RequestParam String action,
            HttpServletRequest request) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, action);
            auditService.logEmployeeAction(authId, "MANUAL_AUDIT_LOG_CREATED: " + action);
        }

        return ResponseEntity.ok().build();
    }
}
