package com.bivolaris.centralbank.services.impl;

import com.bivolaris.centralbank.dtos.BankDto;
import com.bivolaris.centralbank.dtos.BankTransferRequest;
import com.bivolaris.centralbank.dtos.BankTransferResponse;
import com.bivolaris.centralbank.dtos.RegisterBankRequest;
import com.bivolaris.centralbank.entities.Bank;
import com.bivolaris.centralbank.entities.BankStatus;
import com.bivolaris.centralbank.exceptions.BankNotFoundException;
import com.bivolaris.centralbank.exceptions.ValidationException;
import com.bivolaris.centralbank.mappers.BankMapper;
import com.bivolaris.centralbank.repositories.BankRepository;
import com.bivolaris.centralbank.services.BankService;
import com.bivolaris.centralbank.services.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class BankServiceImpl implements BankService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    public BankTransferResponse requestTransferApproval(Bank targetBank, BankTransferRequest request) {
        try {
            String apiUrl = targetBank.getBase_api_endpoint() + "/api/transfers/incoming";
            

            String jwtToken = jwtService.generateBankToken(targetBank);
            Long timestamp = System.currentTimeMillis();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.set("X-Bank-Code", request.getFromBankSwift());
            headers.set("X-Timestamp", String.valueOf(timestamp));
            
            HttpEntity<BankTransferRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<BankTransferResponse> response = restTemplate.postForEntity(
                    apiUrl, entity, BankTransferResponse.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            BankTransferResponse failureResponse = new BankTransferResponse();
            failureResponse.setApproved(false);
            failureResponse.setResponseCode("API_ERROR");
            failureResponse.setResponseMessage("Failed to communicate with bank");
            failureResponse.setErrorDetails(e.getMessage());
            failureResponse.setTimestamp(System.currentTimeMillis());
            return failureResponse;
        }
    }


    public void registerBank(RegisterBankRequest registerBankRequest) {
        try {
        var newBank = new Bank();
        newBank.setName(registerBankRequest.getName());
        newBank.setSwift(registerBankRequest.getSwift());
        newBank.setBase_api_endpoint(registerBankRequest.getBase_api_endpoint());
        newBank.setStatus(BankStatus.ACTIVE);
        newBank.setCreated_at(LocalDateTime.now());
        newBank.setUpdated_at(LocalDateTime.now());


            bankRepository.save(newBank);
        }catch (ValidationException e){
            throw new ValidationException("Bank registration failed for '" +
            registerBankRequest.getName() + "': " + e.getMessage());
        }

    }

    public boolean updateBankDetails(RegisterBankRequest registerBankRequest) {
        var bank = bankRepository.findByBankName(registerBankRequest.getName()).orElse(null);
        if (bank == null) {
            throw new BankNotFoundException("Bank not found");
        }
        bank.setName(registerBankRequest.getName());
        bank.setSwift(registerBankRequest.getSwift());
        bank.setBase_api_endpoint(registerBankRequest.getBase_api_endpoint());

        bankRepository.save(bank);
        return true;
    }

    public List<BankDto> getAllBanks(){
        return bankRepository.findAll()
                .stream()
                .map(bankMapper::toBankDto)
                .toList();

    }

    public BankDto getBankByName(String name){
        var bank = bankRepository.findByBankName(name).orElse(null);
        if (bank == null) {
            throw new BankNotFoundException("Bank not found");
        }
        return bankMapper.toBankDto(bank);
    }


}
