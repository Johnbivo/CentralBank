package com.bivolaris.centralbank.services.impl;

import com.bivolaris.centralbank.dtos.BankTransferRequest;
import com.bivolaris.centralbank.dtos.BankTransferResponse;
import com.bivolaris.centralbank.entities.Bank;
import com.bivolaris.centralbank.services.BankService;
import com.bivolaris.centralbank.services.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Service
public class BankServiceImpl implements BankService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

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


}
