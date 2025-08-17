package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.AccountStatus;
import com.bivolaris.centralbank.entities.CurrencyEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountDetailsRequest {

    private UUID accountId;
    private String accountNumber;
    private String bankName;
    private String accountHolderName;
    private String accountType;
    private BigDecimal balance;
    private CurrencyEnum currency;
    private AccountStatus status;

}
