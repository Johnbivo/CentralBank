package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.AccountAllDto;
import com.bivolaris.centralbank.dtos.AccountDetailsRequest;
import com.bivolaris.centralbank.dtos.CreateAccountRequest;



public interface AccountService {


    public AccountDetailsRequest getAccountDetails(String accountNumber);
    public AccountAllDto createAccount(CreateAccountRequest request);

}
