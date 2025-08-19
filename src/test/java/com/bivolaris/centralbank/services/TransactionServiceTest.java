package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.entities.*;
import com.bivolaris.centralbank.exceptions.InsufficientFundsException;
import com.bivolaris.centralbank.mappers.TransactionMapper;
import com.bivolaris.centralbank.repositories.TransactionData;
import com.bivolaris.centralbank.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest validTransactionRequest;
    private Account fromAccount;
    private Account toAccount;
    private Bank fromBank;
    private Bank toBank;
    private Transaction transaction;
    private TransactionData transactionData;

    @BeforeEach
    void setUp() {

        validTransactionRequest = new TransactionRequest();
        validTransactionRequest.setAccountNumber("123456789");
        validTransactionRequest.setToAccountNumber("987654321");
        validTransactionRequest.setBankName("Test Bank");
        validTransactionRequest.setToBankName("Destination Bank");
        validTransactionRequest.setAmount(new BigDecimal("100.00"));
        validTransactionRequest.setCurrency(CurrencyEnum.USD);
        validTransactionRequest.setMessage("Test transaction");
        validTransactionRequest.setAccountHolderName("John Doe");


        fromAccount = new Account();
        fromAccount.setId(UUID.randomUUID());
        fromAccount.setAccountNumber("123456789");
        fromAccount.setBalance(new BigDecimal("500.00"));
        fromAccount.setAccountHolderName("John Doe");

        // Set up to account
        toAccount = new Account();
        toAccount.setId(UUID.randomUUID());
        toAccount.setAccountNumber("987654321");
        toAccount.setBalance(new BigDecimal("200.00"));
        toAccount.setAccountHolderName("Jane Smith");

        // Set up banks
        fromBank = new Bank();
        fromBank.setId(UUID.randomUUID());
        fromBank.setName("Test Bank");
        fromBank.setSwift("TESTUS33");

        toBank = new Bank();
        toBank.setId(UUID.randomUUID());
        toBank.setName("Destination Bank");
        toBank.setSwift("DESTUS33");

        // Set up transaction
        transaction = new Transaction();
        transaction.setAmount(validTransactionRequest.getAmount());
        transaction.setCurrency(validTransactionRequest.getCurrency());
        transaction.setMessage(validTransactionRequest.getMessage());

        // Mock TransactionData
        transactionData = new TransactionData() {
            @Override
            public Account getFromAccount() {
                return fromAccount;
            }

            @Override
            public Account getToAccount() {
                return toAccount;
            }

            @Override
            public Bank getFromBank() {
                return fromBank;
            }

            @Override
            public Bank getToBank() {
                return toBank;
            }
        };
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        when(transactionRepository.findTransactionData(
                validTransactionRequest.getAccountNumber(),
                validTransactionRequest.getToAccountNumber(),
                validTransactionRequest.getBankName(),
                validTransactionRequest.getToBankName()
        )).thenReturn(Optional.of(transactionData));

        when(transactionMapper.toEntityTransaction(validTransactionRequest))
                .thenReturn(transaction);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        // Act
        boolean result = transactionService.createTransaction(validTransactionRequest);

        // Assert
        assertTrue(result);

        // Verify that the transaction was properly configured
        verify(transactionRepository).save(argThat(savedTransaction -> {
            return savedTransaction.getFromAccount().equals(fromAccount) &&
                   savedTransaction.getToAccount().equals(toAccount) &&
                   savedTransaction.getFromBank().equals(fromBank) &&
                   savedTransaction.getToBank().equals(toBank) &&
                   savedTransaction.getStatus().equals(TransactionStatus.PENDING) &&
                   savedTransaction.getInitiatedAt() != null;
        }));

        // Verify repository and mapper calls
        verify(transactionRepository).findTransactionData(
                validTransactionRequest.getAccountNumber(),
                validTransactionRequest.getToAccountNumber(),
                validTransactionRequest.getBankName(),
                validTransactionRequest.getToBankName()
        );
        verify(transactionMapper).toEntityTransaction(validTransactionRequest);
    }

    @Test
    void createTransaction_InsufficientFunds_ThrowsException() {
        // Arrange - set balance lower than transaction amount
        fromAccount.setBalance(new BigDecimal("50.00"));

        when(transactionRepository.findTransactionData(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(Optional.of(transactionData));

        // Act & Assert
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> transactionService.createTransaction(validTransactionRequest)
        );

        // Verify exception details
        assertNotNull(exception);
        
        // Verify that save was never called
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(transactionMapper, never()).toEntityTransaction(any(TransactionRequest.class));
    }

    @Test
    void createTransaction_InvalidTransactionData_ReturnsFalse() {
        // Arrange - return empty optional to simulate no matching accounts/banks
        when(transactionRepository.findTransactionData(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(Optional.empty());

        // Act
        boolean result = transactionService.createTransaction(validTransactionRequest);

        // Assert
        assertFalse(result);

        // Verify that save and mapper were never called
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(transactionMapper, never()).toEntityTransaction(any(TransactionRequest.class));
    }

    @Test
    void createTransaction_ExactBalanceAmount_Success() {
        // Arrange - set balance exactly equal to transaction amount
        fromAccount.setBalance(new BigDecimal("100.00"));

        when(transactionRepository.findTransactionData(
                validTransactionRequest.getAccountNumber(),
                validTransactionRequest.getToAccountNumber(),
                validTransactionRequest.getBankName(),
                validTransactionRequest.getToBankName()
        )).thenReturn(Optional.of(transactionData));

        when(transactionMapper.toEntityTransaction(validTransactionRequest))
                .thenReturn(transaction);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        // Act
        boolean result = transactionService.createTransaction(validTransactionRequest);

        // Assert
        assertTrue(result);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_VerifyTransactionTimestamp() {
        // Arrange
        LocalDateTime beforeTransaction = LocalDateTime.now();

        when(transactionRepository.findTransactionData(
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(Optional.of(transactionData));

        when(transactionMapper.toEntityTransaction(validTransactionRequest))
                .thenReturn(transaction);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        // Act
        transactionService.createTransaction(validTransactionRequest);

        LocalDateTime afterTransaction = LocalDateTime.now();

        // Assert
        verify(transactionRepository).save(argThat(savedTransaction -> {
            LocalDateTime initiatedAt = savedTransaction.getInitiatedAt();
            return initiatedAt != null &&
                   !initiatedAt.isBefore(beforeTransaction) &&
                   !initiatedAt.isAfter(afterTransaction);
        }));
    }
}
