package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.BankTransferRequest;
import com.bivolaris.centralbank.dtos.BankTransferResponse;
import com.bivolaris.centralbank.entities.Transaction;
import com.bivolaris.centralbank.entities.TransactionStatus;
import com.bivolaris.centralbank.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class TransactionProcessingService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final BankService bankService;


    @Scheduled(fixedDelay = 30000) // 30 seconds
    @Transactional
    public void processClearedTransactions() {
        try {

            List<Transaction> clearedTransactions = transactionRepository
                    .findPendingTransactionsUpdatedRecently();

            if (clearedTransactions.isEmpty()) {
                return;
            }

            log.info("Found {} cleared fraud transactions to process", clearedTransactions.size());

            for (Transaction transaction : clearedTransactions) {
                try {
                    log.info("Resuming transaction processing for transaction ID: {}", transaction.getId());
                    resumeTransactionProcessing(transaction);
                } catch (Exception e) {
                    log.error("Failed to resume transaction {}: {}", transaction.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in processClearedTransactions: {}", e.getMessage());
        }
    }


    @Transactional
    public void resumeTransactionProcessing(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.warn("Transaction {} is not in PENDING state, skipping", transaction.getId());
            return;
        }

        boolean sameBank = transaction.getFromBank().getId().equals(transaction.getToBank().getId());

        if (sameBank) {
            processSameBankTransfer(transaction);
        } else {
            processInterBankTransfer(transaction);
        }
    }

    private void processSameBankTransfer(Transaction transaction) {
        try {
            boolean success = transactionService.completeApprovedTransaction(transaction);
            
            if (success) {
                log.info("Successfully completed cleared transaction: {}", transaction.getId());
            } else {
                log.warn("Failed to complete cleared transaction: {}", transaction.getId());
            }
        } catch (Exception e) {
            log.error("Error processing same-bank transfer {}: {}", transaction.getId(), e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        }
    }



    // Method to process  cleared interbank requests that were previously flagged as fraudulent
    // Sends a request to the target bank again and waits a response.
    private void processInterBankTransfer(Transaction transaction) {
        try {
            log.info("Re-requesting approval for inter-bank transaction: {}", transaction.getId());

            BankTransferRequest bankRequest = createBankTransferRequest(transaction);
            BankTransferResponse response = bankService.requestTransferApproval(
                    transaction.getToBank(), bankRequest
            );
            
            if (response.isApproved()) {
                log.info("Inter-bank transaction {} re-approved, completing transfer", transaction.getId());
                boolean success = transactionService.completeApprovedTransaction(transaction);
                
                if (success) {
                    log.info("Successfully completed inter-bank transaction: {}", transaction.getId());
                } else {
                    log.warn("Failed to complete inter-bank transaction: {}", transaction.getId());
                }
            } else {
                log.warn("Inter-bank transaction {} denied on re-approval: {}", 
                        transaction.getId(), response.getResponseMessage());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
            }
        } catch (Exception e) {
            log.error("Error processing inter-bank transfer {}: {}", transaction.getId(), e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        }
    }


    private BankTransferRequest createBankTransferRequest(Transaction transaction) {
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
}
