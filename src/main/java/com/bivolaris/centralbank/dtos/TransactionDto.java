package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.CurrencyEnum;
import com.bivolaris.centralbank.entities.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionDto {

    private UUID id;
    private UUID fromAccountId;
    private UUID toAccountId;
    private UUID fromBankAccountId;
    private UUID toBankAccountId;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private TransactionStatus status;
    private String message;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;


}
