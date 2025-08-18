package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.CurrencyEnum;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class TransactionRequest {

    private String accountNumber;
    private String bankName;
    private String toAccountNumber;
    private String toBankName;
    private String accountHolderName;
    private CurrencyEnum currency;
    private BigDecimal amount;
    private String message;

}