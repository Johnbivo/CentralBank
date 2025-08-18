package com.bivolaris.centralbank.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    private String errorCode;
    private String message;
    private String userMessage;
    private LocalDateTime timestamp;
    private String path;
    private int status;
    
    public ErrorResponse(String errorCode, String message, String userMessage, String path, int status) {
        this.errorCode = errorCode;
        this.message = message;
        this.userMessage = userMessage;
        this.path = path;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
