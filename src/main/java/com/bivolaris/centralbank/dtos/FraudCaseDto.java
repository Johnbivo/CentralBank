package com.bivolaris.centralbank.dtos;

import com.bivolaris.centralbank.entities.FraudStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class FraudCaseDto {
    private UUID id;
    private UUID transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String fromBankName;
    private String toBankName;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private FraudStatus status;
    private String reviewedByEmployeeName;
    private LocalDateTime flaggedAt;
    private LocalDateTime updatedAt;
}
