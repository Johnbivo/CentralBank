package com.bivolaris.centralbank.controllers;


import com.bivolaris.centralbank.dtos.BankDto;
import com.bivolaris.centralbank.dtos.RegisterBankRequest;
import com.bivolaris.centralbank.dtos.FindByBankNameRequest;
import com.bivolaris.centralbank.exceptions.BankNotFoundException;
import com.bivolaris.centralbank.exceptions.ValidationException;
import com.bivolaris.centralbank.services.BankService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/banks")
public class BankController {


    private final BankService bankService;



    @GetMapping
    public ResponseEntity<List<BankDto>> getAllBanks(){
        var banks = bankService.getAllBanks();
        if(banks.isEmpty()){
            return ResponseEntity.noContent().build();
        };
        return ResponseEntity.ok().body(banks);
    }

    @GetMapping("/find-with-name")
    public ResponseEntity<BankDto> getBankByName(FindByBankNameRequest request){
        var bank = bankService.getBankByName(request.getName());
        if(bank == null) throw new BankNotFoundException("Bank not found");
        return ResponseEntity.ok().body(bank);
    }

    @PostMapping("/register-bank")
    public ResponseEntity<Void> registerBank(@RequestBody RegisterBankRequest registerBankRequest){

        try{
            bankService.registerBank(registerBankRequest);
        }catch(Exception e){
            throw new ValidationException("Invalid Bank Registration Details");
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PutMapping("/update-details")
    public ResponseEntity<Void> updateBankDetails(@RequestBody RegisterBankRequest registerBankRequest){
        try{
            bankService.updateBankDetails(registerBankRequest);
        }catch(Exception e){
            throw new BankNotFoundException("Bank not found");
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }






    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(ValidationException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BankNotFoundException.class)
    public ResponseEntity<String> handleBankNotFoundException(BankNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}
