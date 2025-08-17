package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.dtos.AccountAllDto;
import com.bivolaris.centralbank.dtos.AccountDetailsRequest;
import com.bivolaris.centralbank.dtos.CreateAccountRequest;
import com.bivolaris.centralbank.entities.Account;
import com.bivolaris.centralbank.entities.AccountStatus;
import com.bivolaris.centralbank.mappers.AccountMapper;
import com.bivolaris.centralbank.repositories.AccountRepository;
import com.bivolaris.centralbank.repositories.BankRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;


@AllArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final BankRepository bankRepository;
    private GenerateContent generateContent;

    public AccountDetailsRequest getAccountDetails(String accountNumber){
        var account = accountRepository.findByAccountNumber(accountNumber);
        return accountMapper.accountDetailsToDto(account);
    }


    public AccountAllDto createAccount(CreateAccountRequest request){


        var bankName = bankRepository.findByBankName(request.getBankName()).orElse(null);
        if(bankName == null){
            return null;
        }
        try {
            Account account = new Account();
            account.setBank(bankName);
            account.setAccountHolderName(request.getAccountHolderName());
            account.setAccountType(String.valueOf(request.getAccountType()));
            account.setBalance(BigDecimal.ZERO);
            account.setAccountNumber(generateContent.generateAccountNumber());
            account.setCurrency(String.valueOf(request.getCurrency()));
            account.setStatus(AccountStatus.INACTIVE.name());
            account.setCreatedAt(Instant.now());
            account.setUpdatedAt(Instant.now());

            accountRepository.save(account);

            return accountMapper.accountAllToDto(account);

        }catch(Exception e){
            return null;
        }


    };





}
