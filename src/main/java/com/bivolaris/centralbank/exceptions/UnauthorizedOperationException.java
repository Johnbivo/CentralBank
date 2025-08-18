package com.bivolaris.centralbank.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedOperationException extends BankingException {

    public UnauthorizedOperationException(String operation) {
        super(
            "UNAUTHORIZED_OPERATION", 
            "Unauthorized operation: " + operation,
            "You are not authorized to perform this operation.",
            HttpStatus.FORBIDDEN
        );
    }
    
    public UnauthorizedOperationException(String operation, String requiredRole) {
        super(
            "UNAUTHORIZED_OPERATION", 
            "Unauthorized operation: " + operation + " (requires " + requiredRole + ")",
            "You are not authorized to perform this operation. Required role: " + requiredRole,
            HttpStatus.FORBIDDEN
        );
    }
}
