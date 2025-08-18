package com.bivolaris.centralbank.controllers;



import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.services.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@AllArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionController {


    private final TransactionService transactionService;


    @PostMapping("/create-transaction")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequest transactionRequest) {
        if(!transactionService.createTransaction(transactionRequest)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("Transaction completed successfully.");

    }

}
