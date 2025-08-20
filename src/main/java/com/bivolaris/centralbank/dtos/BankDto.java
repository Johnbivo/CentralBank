package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.BankStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BankDto {
    private UUID bankId;
    private String bankName;
    private String swift;
    private String bankApiEndpoint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BankStatus bankStatus;

}
