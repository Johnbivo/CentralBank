package com.bivolaris.centralbank.controllers;



import com.bivolaris.centralbank.dtos.TransactionRequest;

import com.bivolaris.centralbank.services.AuditService;
import com.bivolaris.centralbank.services.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionController {


    private final TransactionService transactionService;
    private final AuditService auditService;


    @PostMapping("/create-transaction")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequest transactionRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        transactionService.createTransaction(transactionRequest);
        

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "TRANSACTION_CREATE_SUCCESS");
        }
        
        return ResponseEntity.ok("Transaction completed successfully.");
    }

}
