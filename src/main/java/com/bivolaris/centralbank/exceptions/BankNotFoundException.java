package com.bivolaris.centralbank.exceptions;

import org.springframework.http.HttpStatus;

public class BankNotFoundException extends BankingException {

    public BankNotFoundException(String bankName) {
        super(
            "BANK_NOT_FOUND", 
            "Bank not found: " + bankName,
            "The specified bank could not be found. Please verify the bank name or SWIFT code.",
            HttpStatus.NOT_FOUND
        );
    }
    
    public BankNotFoundException(String bankName, String swift) {
        super(
            "BANK_NOT_FOUND", 
            "Bank not found: " + bankName + " (SWIFT: " + swift + ")",
            "The specified bank could not be found. Please verify the bank name or SWIFT code.",
            HttpStatus.NOT_FOUND
        );
    }
}
