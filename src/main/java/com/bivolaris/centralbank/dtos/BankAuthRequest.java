package com.bivolaris.centralbank.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BankAuthRequest {
    
    @NotBlank(message = "SWIFT code is required")
    @Size(min = 8, max = 11, message = "SWIFT code must be 8-11 characters")
    private String swift;
}
