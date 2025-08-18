package com.bivolaris.centralbank.dtos;

import lombok.Data;

@Data
public class BankTransferResponse {
    private boolean approved;
    private String responseCode;
    private String responseMessage;
    private String errorDetails;
    private Long timestamp;
}
