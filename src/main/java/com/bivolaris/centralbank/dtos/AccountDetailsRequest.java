package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.CurrencyEnum;
import lombok.Data;



@Data
public class AccountDetailsRequest {


    private String accountNumber;
    private String bankName;
    private String accountHolderName;
    private CurrencyEnum currency;


}
