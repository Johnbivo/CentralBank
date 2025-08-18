package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final AuditService auditService;

    /**
     * Simple test endpoint to verify rate limiting
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "TEST_PING_ACCESSED");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "ok");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint that simulates heavy usage
     */
    @GetMapping("/heavy")
    public ResponseEntity<Map<String, Object>> heavy() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "TEST_HEAVY_ACCESSED");
        }

        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Heavy operation completed");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }
}
