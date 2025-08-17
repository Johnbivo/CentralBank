package com.bivolaris.centralbank.controllers;


import com.bivolaris.centralbank.dtos.AccountAllDto;
import com.bivolaris.centralbank.dtos.AccountDetailsRequest;
import com.bivolaris.centralbank.dtos.CreateAccountRequest;
import com.bivolaris.centralbank.entities.Account;
import com.bivolaris.centralbank.services.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {


    private final AccountService accountService;



    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDetailsRequest> getAccountDetails(@PathVariable String accountNumber){
        var account = accountService.getAccountDetails(accountNumber);
        return ResponseEntity.ok().body(account);
    }


    @PostMapping("/create")
    public ResponseEntity<AccountAllDto> createAccount(@RequestBody CreateAccountRequest createAccountRequest){
        var account = accountService.createAccount(createAccountRequest);
        if(account == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(account);
    }


}
