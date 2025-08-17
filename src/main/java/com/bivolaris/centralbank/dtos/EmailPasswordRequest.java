package com.bivolaris.centralbank.dtos;


import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailPasswordRequest {

    @Email
    private String email;
}
