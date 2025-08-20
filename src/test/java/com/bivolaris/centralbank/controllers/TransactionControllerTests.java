package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.entities.CurrencyEnum;
import com.bivolaris.centralbank.exceptions.AccountNotFoundException;
import com.bivolaris.centralbank.exceptions.FraudDetectedException;
import com.bivolaris.centralbank.exceptions.InsufficientFundsException;
import com.bivolaris.centralbank.services.AuditService;
import com.bivolaris.centralbank.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTests {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuditService auditService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new TransactionRequest();
        validRequest.setAccountNumber("ACC123456789");
        validRequest.setBankName("Test Bank");
        validRequest.setToAccountNumber("ACC987654321");
        validRequest.setToBankName("Destination Bank");
        validRequest.setAccountHolderName("John Doe");
        validRequest.setCurrency(CurrencyEnum.USD);
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setMessage("Test transfer");
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        // Given
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(123L);
        SecurityContextHolder.setContext(securityContext);

        // When
        ResponseEntity<String> response = transactionController.createTransaction(validRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction completed successfully.", response.getBody());
        verify(transactionService).createTransaction(validRequest);
        verify(auditService).logEmployeeAction(123L, "TRANSACTION_CREATE_SUCCESS");
    }

    @Test
    void shouldHandleTransactionWithoutAuthentication() {
        // Given
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(true);
        SecurityContextHolder.clearContext();

        // When
        ResponseEntity<String> response = transactionController.createTransaction(validRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction completed successfully.", response.getBody());
        verify(transactionService).createTransaction(validRequest);
        verify(auditService, never()).logEmployeeAction(anyLong(), anyString());
    }

    @Test
    void shouldHandleInsufficientFundsException() {
        // Given
        when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenThrow(new InsufficientFundsException("Not enough funds"));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> {
            transactionController.createTransaction(validRequest);
        });

        verify(transactionService).createTransaction(validRequest);
        verify(auditService, never()).logEmployeeAction(anyLong(), anyString());
    }

    @Test
    void shouldHandleFraudDetectedException() {
        // Given
        when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenThrow(new FraudDetectedException("Fraud detected"));

        // When & Then
        assertThrows(FraudDetectedException.class, () -> {
            transactionController.createTransaction(validRequest);
        });

        verify(transactionService).createTransaction(validRequest);
        verify(auditService, never()).logEmployeeAction(anyLong(), anyString());
    }

    @Test
    void shouldHandleAccountNotFoundException() {
        // Given
        when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenThrow(new AccountNotFoundException("Account not found"));

        // When & Then
        assertThrows(AccountNotFoundException.class, () -> {
            transactionController.createTransaction(validRequest);
        });

        verify(transactionService).createTransaction(validRequest);
        verify(auditService, never()).logEmployeeAction(anyLong(), anyString());
    }

    @Test
    void shouldHandleNullTransactionRequest() {
        // Given
        when(transactionService.createTransaction(null)).thenReturn(true);
        SecurityContextHolder.clearContext();

        // When
        ResponseEntity<String> response = transactionController.createTransaction(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction completed successfully.", response.getBody());
        verify(transactionService).createTransaction(null);
        verify(auditService, never()).logEmployeeAction(anyLong(), anyString());
    }

    @Test
    void shouldHandleAuthenticationWithNonLongPrincipal() {
        // Given
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("string-principal");
        SecurityContextHolder.setContext(securityContext);

        // When
        ResponseEntity<String> response = transactionController.createTransaction(validRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(transactionService).createTransaction(validRequest);
        verify(auditService, never()).logEmployeeAction(anyLong(), anyString());
    }
}