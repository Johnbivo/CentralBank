package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.AccountStatus;
import com.bivolaris.centralbank.entities.AccountTypes;
import com.bivolaris.centralbank.entities.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class AccountAllDto {

    private UUID accountId;
    private String accountNumber;
    private String accountHolderName;
    private AccountTypes accountType;
    private BigDecimal balance;
    private CurrencyEnum currency;
    private AccountStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Athens")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Athens")
    private Instant updatedAt;

}
