package com.bivolaris.centralbank.exceptions;

import org.springframework.http.HttpStatus;

public class TransactionFailedException extends BankingException {

    public TransactionFailedException(String reason) {
        super(
            "TRANSACTION_FAILED", 
            "Transaction failed: " + reason,
            "The transaction could not be completed. " + reason,
            HttpStatus.BAD_REQUEST
        );
    }
    
    public TransactionFailedException(String transactionId, String reason) {
        super(
            "TRANSACTION_FAILED", 
            "Transaction " + transactionId + " failed: " + reason,
            "The transaction could not be completed. " + reason,
            HttpStatus.BAD_REQUEST
        );
    }
}
