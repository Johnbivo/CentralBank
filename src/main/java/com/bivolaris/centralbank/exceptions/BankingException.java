package com.bivolaris.centralbank.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public abstract class BankingException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String userMessage;
    
    protected BankingException(String errorCode, String message, String userMessage, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.httpStatus = httpStatus;
    }
    
    protected BankingException(String errorCode, String message, String userMessage, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.httpStatus = httpStatus;
    }
}
