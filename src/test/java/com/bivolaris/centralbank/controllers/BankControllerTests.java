package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.dtos.BankDto;
import com.bivolaris.centralbank.dtos.FindByBankNameRequest;
import com.bivolaris.centralbank.dtos.RegisterBankRequest;
import com.bivolaris.centralbank.entities.BankStatus;
import com.bivolaris.centralbank.exceptions.BankNotFoundException;
import com.bivolaris.centralbank.exceptions.ValidationException;
import com.bivolaris.centralbank.services.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankControllerTests {

    @Mock
    private BankService bankService;

    @InjectMocks
    private BankController bankController;

    private RegisterBankRequest validRegisterRequest;
    private FindByBankNameRequest validFindRequest;
    private BankDto sampleBankDto;
    private List<BankDto> bankList;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterBankRequest();
        validRegisterRequest.setName("Test Bank");
        validRegisterRequest.setSwift("TESTBANK01");
        validRegisterRequest.setBase_api_endpoint("https://api.testbank.com");

        validFindRequest = new FindByBankNameRequest();
        validFindRequest.setName("Test Bank");

        sampleBankDto = new BankDto();
        sampleBankDto.setBankId(UUID.randomUUID());
        sampleBankDto.setBankName("Test Bank");
        sampleBankDto.setSwift("TESTBANK01");
        sampleBankDto.setBankApiEndpoint("https://api.testbank.com");
        sampleBankDto.setBankStatus(BankStatus.ACTIVE);
        sampleBankDto.setCreatedAt(LocalDateTime.now());
        sampleBankDto.setUpdatedAt(LocalDateTime.now());

        bankList = Arrays.asList(sampleBankDto);
    }

    @Test
    void shouldGetAllBanksSuccessfully() {
        // Given
        when(bankService.getAllBanks()).thenReturn(bankList);

        // When
        ResponseEntity<List<BankDto>> response = bankController.getAllBanks();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bankList, response.getBody());
        verify(bankService).getAllBanks();
    }

    @Test
    void shouldReturnNoContentWhenNoBanksFound() {
        // Given
        when(bankService.getAllBanks()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<BankDto>> response = bankController.getAllBanks();

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(bankService).getAllBanks();
    }

    @Test
    void shouldFindBankByNameSuccessfully() {
        // Given
        when(bankService.getBankByName(anyString())).thenReturn(sampleBankDto);

        // When
        ResponseEntity<BankDto> response = bankController.getBankByName(validFindRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleBankDto, response.getBody());
        verify(bankService).getBankByName("Test Bank");
    }

    @Test
    void shouldThrowBankNotFoundExceptionWhenBankNotExists() {
        // Given
        when(bankService.getBankByName(anyString()))
                .thenThrow(new BankNotFoundException("Test Bank"));

        // When & Then
        assertThrows(BankNotFoundException.class, () -> {
            bankController.getBankByName(validFindRequest);
        });

        verify(bankService).getBankByName("Test Bank");
    }

    @Test
    void shouldRegisterBankSuccessfully() {
        // Given
        doNothing().when(bankService).registerBank(any(RegisterBankRequest.class));

        // When
        ResponseEntity<Void> response = bankController.registerBank(validRegisterRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(bankService).registerBank(validRegisterRequest);
    }

    @Test
    void shouldThrowValidationExceptionForInvalidRegisterData() {
        // Given
        doThrow(new ValidationException("Invalid data")).when(bankService).registerBank(any(RegisterBankRequest.class));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            bankController.registerBank(validRegisterRequest);
        });

        verify(bankService).registerBank(validRegisterRequest);
    }

    @Test
    void shouldUpdateBankDetailsSuccessfully() {
        // Given
        when(bankService.updateBankDetails(any(RegisterBankRequest.class))).thenReturn(true);

        // When
        ResponseEntity<Void> response = bankController.updateBankDetails(validRegisterRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bankService).updateBankDetails(validRegisterRequest);
    }

    @Test
    void shouldHandleUpdateException() {
        // Given
        when(bankService.updateBankDetails(any(RegisterBankRequest.class)))
                .thenThrow(new RuntimeException("Bank not found"));

        // When & Then
        assertThrows(BankNotFoundException.class, () -> {
            bankController.updateBankDetails(validRegisterRequest);
        });

        verify(bankService).updateBankDetails(validRegisterRequest);
    }

    @Test
    void shouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("Invalid data");

        // When
        ResponseEntity<String> response = bankController.handleValidationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error: Invalid data", response.getBody());
    }

    @Test
    void shouldHandleBankNotFoundException() {
        // Given
        BankNotFoundException exception = new BankNotFoundException("Unknown Bank");

        // When
        ResponseEntity<String> response = bankController.handleBankNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Bank not found: Unknown Bank", response.getBody());
    }

    @Test
    void shouldHandleNullRegisterRequest() {
        // Given - registerBank is void, so doNothing() is correct
        doNothing().when(bankService).registerBank(null);

        // When
        ResponseEntity<Void> response = bankController.registerBank(null);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(bankService).registerBank(null);
    }
}