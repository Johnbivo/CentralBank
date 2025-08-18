package com.bivolaris.centralbank.dtos;

import com.bivolaris.centralbank.entities.CurrencyEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BankTransferRequest {
    private UUID transactionId;
    private String fromBankSwift;
    private String fromBankName;
    private String toBankSwift;
    private String toBankName;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String accountHolderName;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private String message;
}
