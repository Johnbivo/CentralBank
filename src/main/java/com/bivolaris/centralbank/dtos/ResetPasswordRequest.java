package com.bivolaris.centralbank.dtos;


import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String Password;
    private String confirmPassword;

}
