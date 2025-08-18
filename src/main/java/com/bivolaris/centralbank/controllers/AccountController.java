package com.bivolaris.centralbank.controllers;


import com.bivolaris.centralbank.dtos.AccountAllDto;
import com.bivolaris.centralbank.dtos.AccountDetailsRequest;
import com.bivolaris.centralbank.dtos.CreateAccountRequest;
import com.bivolaris.centralbank.exceptions.ValidationException;
import com.bivolaris.centralbank.services.AccountService;
import com.bivolaris.centralbank.services.AuditService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@AllArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {


    private final AccountService accountService;
    private final AuditService auditService;



    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDetailsRequest> getAccountDetails(@PathVariable String accountNumber){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "ACCOUNT_DETAILS_ACCESSED");
        }
        
        var account = accountService.getAccountDetails(accountNumber);
        return ResponseEntity.ok().body(account);
    }


    @PostMapping("/create")
    public ResponseEntity<AccountAllDto> createAccount(@RequestBody CreateAccountRequest createAccountRequest){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        var account = accountService.createAccount(createAccountRequest);

        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "ACCOUNT_CREATE_SUCCESS");
        }
        
        return ResponseEntity.ok().body(account);
    }


}
