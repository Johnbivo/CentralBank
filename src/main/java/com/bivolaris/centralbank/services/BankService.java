package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.BankTransferRequest;
import com.bivolaris.centralbank.dtos.BankTransferResponse;
import com.bivolaris.centralbank.entities.Bank;

public interface BankService {


    public BankTransferResponse requestTransferApproval(Bank targetBank, BankTransferRequest request);

}
