package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.entities.Transaction;

public interface TransactionService {

    public boolean createTransaction(TransactionRequest transactionRequest);
    public boolean completeApprovedTransaction(Transaction transaction);
}
