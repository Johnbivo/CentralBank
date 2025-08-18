package com.bivolaris.centralbank.exceptions;

import org.springframework.http.HttpStatus;
import java.math.BigDecimal;

public class InsufficientFundsException extends BankingException {

    public InsufficientFundsException(String accountNumber, BigDecimal balance, BigDecimal requestedAmount) {
        super(
            "INSUFFICIENT_FUNDS",
            String.format("Insufficient funds in account %s. Balance: %s, Requested: %s",
                    accountNumber, balance, requestedAmount),
            String.format("Insufficient funds. Available balance: %s, Requested amount: %s",
                    balance, requestedAmount),
            HttpStatus.BAD_REQUEST
        );
    }
    
    public InsufficientFundsException(String message) {
        super(
            "INSUFFICIENT_FUNDS",
            message,
            "Insufficient funds for this transaction.",
            HttpStatus.BAD_REQUEST
        );
    }
}