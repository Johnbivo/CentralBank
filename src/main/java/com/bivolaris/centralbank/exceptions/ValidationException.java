package com.bivolaris.centralbank.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends BankingException {

    public ValidationException(String field, String message) {
        super(
            "VALIDATION_ERROR", 
            "Validation error in field '" + field + "': " + message,
            "Invalid input: " + message,
            HttpStatus.BAD_REQUEST
        );
    }
    
    public ValidationException(String message) {
        super(
            "VALIDATION_ERROR", 
            "Validation error: " + message,
            message,
            HttpStatus.BAD_REQUEST
        );
    }
}
