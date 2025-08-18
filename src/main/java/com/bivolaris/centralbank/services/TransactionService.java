package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.dtos.BankTransferRequest;
import com.bivolaris.centralbank.dtos.BankTransferResponse;
import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.entities.Account;
import com.bivolaris.centralbank.entities.Transaction;
import com.bivolaris.centralbank.entities.TransactionStatus;
import com.bivolaris.centralbank.exceptions.InsufficientFundsException;
import com.bivolaris.centralbank.mappers.TransactionMapper;
import com.bivolaris.centralbank.repositories.AccountRepository;
import com.bivolaris.centralbank.repositories.TransactionData;
import com.bivolaris.centralbank.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@AllArgsConstructor
@Service
public class TransactionService {



    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BankService bankService;
    private final FraudDetectionService fraudDetectionService;


    @Transactional
    public boolean createTransaction(TransactionRequest transactionRequest) {

        TransactionData data = transactionRepository.findTransactionData(
                transactionRequest.getAccountNumber(),
                transactionRequest.getToAccountNumber(),
                transactionRequest.getBankName(),
                transactionRequest.getToBankName()
        ).orElse(null);

        if (data == null) {
            return false;
        }

        Account fromAccount = data.getFromAccount();
        if (fromAccount.getBalance().compareTo(transactionRequest.getAmount()) < 0) {

            throw new InsufficientFundsException(fromAccount.getAccountNumber(),
                    fromAccount.getBalance(),
                    transactionRequest.getAmount()
            );
        }

        var transaction = transactionMapper.toEntityTransaction(transactionRequest);


        transaction.setFromAccount(data.getFromAccount());
        transaction.setToAccount(data.getToAccount());
        transaction.setFromBank(data.getFromBank());
        transaction.setToBank(data.getToBank());


        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setInitiatedAt(LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        // Perform fraud detection before processing the transaction
        boolean isFraudulent = fraudDetectionService.detectFraud(transaction);
        if (isFraudulent) {
            // Transaction has been flagged for fraud - stop processing
            return false;
        }

        boolean sameBank = data.getFromBank().getId().equals(data.getToBank().getId());



        if (sameBank) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(transactionRequest.getAmount()));
            Account toAccount = data.getToAccount();
            toAccount.setBalance(toAccount.getBalance().add(transactionRequest.getAmount()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        }
        //different bank
        else {

            BankTransferRequest bankRequest = getBankTransferRequest(transaction);
            BankTransferResponse response = bankService.requestTransferApproval(
                    transaction.getToBank(), bankRequest
            );

            if (response.isApproved()) {

                return completeApprovedTransaction(transaction);
            }
            else {

                System.out.println("Bank transfer denied: " + response.getResponseMessage());
                
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                return false;
            }
        }


        return true;
    }


    private static BankTransferRequest getBankTransferRequest(Transaction transaction) {
        BankTransferRequest bankRequest = new BankTransferRequest();
        bankRequest.setTransactionId(transaction.getId());
        bankRequest.setFromBankSwift(transaction.getFromBank().getSwift());
        bankRequest.setFromBankName(transaction.getFromBank().getName());
        bankRequest.setToBankSwift(transaction.getToBank().getSwift());
        bankRequest.setToBankName(transaction.getToBank().getName());
        bankRequest.setFromAccountNumber(transaction.getFromAccount().getAccountNumber());
        bankRequest.setToAccountNumber(transaction.getToAccount().getAccountNumber());
        bankRequest.setAccountHolderName(transaction.getToAccount().getAccountHolderName());
        bankRequest.setAmount(transaction.getAmount());
        bankRequest.setCurrency(transaction.getCurrency());
        bankRequest.setMessage(transaction.getMessage());
        return bankRequest;
    }


    @Transactional
    public boolean completeApprovedTransaction(Transaction transaction) {
        Account fromAccount = transaction.getFromAccount();
        Account toAccount = transaction.getToAccount();


        if (fromAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            return false;
        }


        fromAccount.setBalance(fromAccount.getBalance().subtract(transaction.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transaction.getAmount()));


        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);


        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return true;
    }








}
