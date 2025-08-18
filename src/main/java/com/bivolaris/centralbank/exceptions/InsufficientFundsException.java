package com.bivolaris.centralbank.exceptions;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }


    public InsufficientFundsException(String accountNumber, BigDecimal balance, BigDecimal requestedAmount) {
        super(String.format("Insufficient funds in account %s. Balance: %s, Requested: %s",
                accountNumber, balance, requestedAmount));
    }
}