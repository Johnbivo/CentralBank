package com.bivolaris.centralbank.dtos;


import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;

}
