package com.bivolaris.centralbank.dtos;


import com.bivolaris.centralbank.entities.AccountTypes;
import lombok.Data;

import java.util.Currency;

@Data
public class CreateAccountRequest {

    private String bankName;
    private String accountHolderName;
    private Currency currency;
    private AccountTypes accountType;

}
