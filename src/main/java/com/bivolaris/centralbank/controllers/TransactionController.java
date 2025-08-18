package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.services.AuditService;
import com.bivolaris.centralbank.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "Operations for creating and managing financial transactions")
public class TransactionController {


    private final TransactionService transactionService;
    private final AuditService auditService;


    @PostMapping("/create-transaction")
    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new financial transaction between accounts, supporting multi-currency transfers with automatic conversion and fraud detection"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
        @ApiResponse(responseCode = "403", description = "Insufficient funds or fraud detected", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequest transactionRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        transactionService.createTransaction(transactionRequest);
        

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "TRANSACTION_CREATE_SUCCESS");
        }
        
        return ResponseEntity.ok("Transaction completed successfully.");
    }

}
