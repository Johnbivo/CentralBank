package com.bivolaris.centralbank.dtos;


import lombok.Data;

@Data
public class RegisterBankRequest {

    private String name;
    private String swift;
    private String base_api_endpoint;
}
