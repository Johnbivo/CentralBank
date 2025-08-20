package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.BankDto;
import com.bivolaris.centralbank.dtos.BankTransferRequest;
import com.bivolaris.centralbank.dtos.BankTransferResponse;
import com.bivolaris.centralbank.dtos.RegisterBankRequest;
import com.bivolaris.centralbank.entities.Bank;

import java.util.List;

public interface BankService {


    public BankTransferResponse requestTransferApproval(Bank targetBank, BankTransferRequest request);
    public void registerBank(RegisterBankRequest registerBankRequest);
    public boolean updateBankDetails(RegisterBankRequest registerBankRequest);
    public List<BankDto> getAllBanks();
    public BankDto getBankByName(String name);

}
