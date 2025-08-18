package com.bivolaris.centralbank.controllers;



import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.exceptions.InsufficientFundsException;
import com.bivolaris.centralbank.services.AuditService;
import com.bivolaris.centralbank.services.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
        
        if(!transactionService.createTransaction(transactionRequest)){
            if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
                auditService.logEmployeeAction(authId, "TRANSACTION_CREATE_FAILED");
            }
            return ResponseEntity.badRequest().build();
        }
        

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "TRANSACTION_CREATE_SUCCESS");
        }
        
        return ResponseEntity.ok("Transaction completed successfully.");
    }

    @ExceptionHandler({InsufficientFundsException.class})
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}
