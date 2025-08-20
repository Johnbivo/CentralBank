package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.AccountTypes;
import com.bivolaris.centralbank.entities.CurrencyEnum;
import lombok.Data;

@Data

public class CreateAccountRequest {

    private String bankName;
    private String accountHolderName;
    private CurrencyEnum currency;
    private AccountTypes accountType;

}
