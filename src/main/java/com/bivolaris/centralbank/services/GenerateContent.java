package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.repositories.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@AllArgsConstructor
@Service
public class GenerateContent {

    private final AccountRepository accountRepository;


    public String generateAccountNumber() {
        String accountNumber;
        do {
            long number = 100_000_000_000L + (long) (Math.random() * 899_999_999_999L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

}
