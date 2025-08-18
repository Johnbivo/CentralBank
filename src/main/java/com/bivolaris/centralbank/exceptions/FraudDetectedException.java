package com.bivolaris.centralbank.exceptions;

import org.springframework.http.HttpStatus;

public class FraudDetectedException extends BankingException {

    public FraudDetectedException(String reason) {
        super(
            "FRAUD_DETECTED", 
            "Fraudulent transaction detected: " + reason,
            "This transaction has been flagged for security review. Please contact customer service.",
            HttpStatus.FORBIDDEN
        );
    }
    
    public FraudDetectedException(String transactionId, String reason) {
        super(
            "FRAUD_DETECTED", 
            "Fraudulent transaction detected for ID " + transactionId + ": " + reason,
            "This transaction has been flagged for security review. Please contact customer service.",
            HttpStatus.FORBIDDEN
        );
    }
}
