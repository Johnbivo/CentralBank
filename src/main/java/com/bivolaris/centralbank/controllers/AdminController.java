package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuditService auditService;

    @GetMapping
    public String sayHello(){

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "ADMIN_HELLO_ENDPOINT_ACCESSED");
        }
        
        return "Hello World";
    }
}
