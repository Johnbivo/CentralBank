package com.bivolaris.centralbank.exceptions;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(String accountNumber) {
        super(
            "ACCOUNT_NOT_FOUND", 
            "Account not found: " + accountNumber,
            "The specified account could not be found. Please verify the account number.",
            HttpStatus.NOT_FOUND
        );
    }
}
