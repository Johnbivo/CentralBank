package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.dtos.BankTransferRequest;
import com.bivolaris.centralbank.dtos.BankTransferResponse;
import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.entities.Account;
import com.bivolaris.centralbank.entities.Transaction;
import com.bivolaris.centralbank.entities.TransactionStatus;
import com.bivolaris.centralbank.exceptions.AccountNotFoundException;
import com.bivolaris.centralbank.exceptions.FraudDetectedException;
import com.bivolaris.centralbank.exceptions.InsufficientFundsException;
import com.bivolaris.centralbank.exceptions.TransactionFailedException;
import com.bivolaris.centralbank.mappers.TransactionMapper;
import com.bivolaris.centralbank.repositories.AccountRepository;
import com.bivolaris.centralbank.repositories.TransactionData;
import com.bivolaris.centralbank.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@AllArgsConstructor
@Service
public class TransactionService {



    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BankService bankService;
    private final FraudDetectionService fraudDetectionService;
    private final CurrencyExchangeService currencyExchangeService;


    @Transactional
    public boolean createTransaction(TransactionRequest transactionRequest) {

        TransactionData data = transactionRepository.findTransactionData(
                transactionRequest.getAccountNumber(),
                transactionRequest.getToAccountNumber(),
                transactionRequest.getBankName(),
                transactionRequest.getToBankName()
        ).orElse(null);

        if (data == null) {
            throw new AccountNotFoundException("Source or destination account not found");
        }

        Account fromAccount = data.getFromAccount();
        Account toAccount = data.getToAccount();
        

        BigDecimal amountInFromCurrency;
        try {
            amountInFromCurrency = transactionRequest.getAmount();
            if (transactionRequest.getCurrency() != fromAccount.getCurrency()) {
                amountInFromCurrency = currencyExchangeService.convertCurrency(
                    transactionRequest.getAmount(), 
                    transactionRequest.getCurrency(), 
                    fromAccount.getCurrency()
                );
            }
        } catch (Exception e) {
            throw new TransactionFailedException("Currency conversion failed: " + e.getMessage());
        }
        
        if (fromAccount.getBalance().compareTo(amountInFromCurrency) < 0) {
            throw new InsufficientFundsException(fromAccount.getAccountNumber(),
                    fromAccount.getBalance(),
                    amountInFromCurrency
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


        boolean isFraudulent = fraudDetectionService.detectFraud(transaction);
        if (isFraudulent) {
            throw new FraudDetectedException("Transaction flagged for security review");
        }

        boolean sameBank = data.getFromBank().getId().equals(data.getToBank().getId());



        if (sameBank) {
            try {
                BigDecimal debitAmount = amountInFromCurrency;
                BigDecimal creditAmount = transactionRequest.getAmount();
                if (transactionRequest.getCurrency() != toAccount.getCurrency()) {
                    creditAmount = currencyExchangeService.convertCurrency(
                        transactionRequest.getAmount(),
                        transactionRequest.getCurrency(),
                        toAccount.getCurrency()
                    );
                }
                
                fromAccount.setBalance(fromAccount.getBalance().subtract(debitAmount));
                toAccount.setBalance(toAccount.getBalance().add(creditAmount));

                accountRepository.save(fromAccount);
                accountRepository.save(toAccount);

                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
            } catch (Exception e) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                throw new TransactionFailedException("Transaction processing failed: " + e.getMessage());
            }
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
                throw new TransactionFailedException("Inter-bank transfer was denied: " + response.getResponseMessage());
            }
        }
        return false;
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

        try {

            java.math.BigDecimal debitAmount = transaction.getAmount();
            if (transaction.getCurrency() != fromAccount.getCurrency()) {
                debitAmount = currencyExchangeService.convertCurrency(
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    fromAccount.getCurrency()
                );
            }

            if (fromAccount.getBalance().compareTo(debitAmount) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                return false;
            }


            BigDecimal creditAmount = transaction.getAmount();
            if (transaction.getCurrency() != toAccount.getCurrency()) {
                creditAmount = currencyExchangeService.convertCurrency(
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    toAccount.getCurrency()
                );
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(debitAmount));
            toAccount.setBalance(toAccount.getBalance().add(creditAmount));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            return false;
        }

        return true;
    }









}
