package com.bivolaris.centralbank;

import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.entities.*;
import com.bivolaris.centralbank.repositories.AccountRepository;
import com.bivolaris.centralbank.repositories.BankRepository;
import com.bivolaris.centralbank.repositories.TransactionRepository;
import com.bivolaris.centralbank.services.TransactionService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Manual test for transaction creation functionality.
 * This test creates actual data in your MySQL database and tests the transaction service.
 * Run this test to verify that the transaction creation works end-to-end.
 */
@SpringBootTest
public class ManualTransactionTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Disabled("Manual test - run only when needed for testing transaction functionality")
    @Test
    public void createTestDataAndTestTransaction() {
        System.out.println("=== Creating Test Banks and Accounts ===");
        
        // Create test banks
        Bank sourceBank = createTestBank("Test Source Bank", "TSRCUS33", "https://api.testsourcebank.com");
        Bank destinationBank = createTestBank("Test Destination Bank", "TDSTUS33", "https://api.testdestbank.com");

        // Create test accounts
        Account sourceAccount = createTestAccount("TEST001", sourceBank, "Alice Johnson", new BigDecimal("2000.00"));
        Account destinationAccount = createTestAccount("TEST002", destinationBank, "Bob Smith", new BigDecimal("1000.00"));

        System.out.println("Created source account: " + sourceAccount.getAccountNumber() + 
                         " with balance: $" + sourceAccount.getBalance());
        System.out.println("Created destination account: " + destinationAccount.getAccountNumber() + 
                         " with balance: $" + destinationAccount.getBalance());

        System.out.println("\n=== Testing Transaction Creation ===");

        // Test 1: Successful transaction
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("TEST001");
        request.setBankName("Test Source Bank");
        request.setToAccountNumber("TEST002");
        request.setToBankName("Test Destination Bank");
        request.setAccountHolderName("Alice Johnson");
        request.setAmount(new BigDecimal("150.00"));
        request.setCurrency(CurrencyEnum.USD);
        request.setMessage("Test payment from Alice to Bob");

        boolean result = transactionService.createTransaction(request);
        
        if (result) {
            System.out.println("✅ SUCCESS: Transaction created successfully!");
            
            // Verify the transaction was saved
            var transactions = transactionRepository.findAll();
            Transaction lastTransaction = transactions.get(transactions.size() - 1);
            
            System.out.println("Transaction Details:");
            System.out.println("- ID: " + lastTransaction.getId());
            System.out.println("- From: " + lastTransaction.getFromAccount().getAccountNumber() + 
                             " (" + lastTransaction.getFromBank().getName() + ")");
            System.out.println("- To: " + lastTransaction.getToAccount().getAccountNumber() + 
                             " (" + lastTransaction.getToBank().getName() + ")");
            System.out.println("- Amount: $" + lastTransaction.getAmount());
            System.out.println("- Currency: " + lastTransaction.getCurrency());
            System.out.println("- Status: " + lastTransaction.getStatus());
            System.out.println("- Message: " + lastTransaction.getMessage());
            System.out.println("- Initiated at: " + lastTransaction.getInitiatedAt());
            
        } else {
            System.out.println("❌ FAILED: Transaction creation failed!");
        }

        // Test 2: Insufficient funds
        System.out.println("\n=== Testing Insufficient Funds Scenario ===");
        TransactionRequest largeRequest = new TransactionRequest();
        largeRequest.setAccountNumber("TEST001");
        largeRequest.setBankName("Test Source Bank");
        largeRequest.setToAccountNumber("TEST002");
        largeRequest.setToBankName("Test Destination Bank");
        largeRequest.setAccountHolderName("Alice Johnson");
        largeRequest.setAmount(new BigDecimal("5000.00"));
        largeRequest.setCurrency(CurrencyEnum.USD);
        largeRequest.setMessage("Large payment that should fail");

        try {
            transactionService.createTransaction(largeRequest);
            System.out.println("❌ UNEXPECTED: Large transaction should have failed but didn't!");
        } catch (Exception e) {
            System.out.println("✅ SUCCESS: Insufficient funds exception caught as expected");
            System.out.println("Exception message: " + e.getMessage());
        }

        // Test 3: Non-existent account
        System.out.println("\n=== Testing Non-existent Account Scenario ===");
        TransactionRequest invalidRequest = new TransactionRequest();
        invalidRequest.setAccountNumber("NONEXISTENT");
        invalidRequest.setBankName("Test Source Bank");
        invalidRequest.setToAccountNumber("TEST002");
        invalidRequest.setToBankName("Test Destination Bank");
        invalidRequest.setAccountHolderName("Non Existent");
        invalidRequest.setAmount(new BigDecimal("100.00"));
        invalidRequest.setCurrency(CurrencyEnum.USD);
        invalidRequest.setMessage("This should fail");

        boolean invalidResult = transactionService.createTransaction(invalidRequest);
        if (!invalidResult) {
            System.out.println("✅ SUCCESS: Non-existent account transaction failed as expected");
        } else {
            System.out.println("❌ UNEXPECTED: Non-existent account transaction should have failed!");
        }

        System.out.println("\n=== Test Summary ===");
        System.out.println("All transaction creation tests completed!");
        System.out.println("Check the console output above to verify all scenarios worked correctly.");
        
        // Assert for JUnit
        assertTrue(result, "Main transaction should succeed");
        assertFalse(invalidResult, "Invalid account transaction should fail");
    }

    private Bank createTestBank(String name, String swift, String apiEndpoint) {
        // Check if bank already exists
        var existingBank = bankRepository.findByBankName(name);
        if (existingBank.isPresent()) {
            System.out.println("Bank '" + name + "' already exists, using existing one.");
            return existingBank.get();
        }

        Bank bank = new Bank();
        bank.setName(name);
        bank.setSwift(swift);
        bank.setBase_api_endpoint(apiEndpoint);
        bank.setStatus(BankStatus.ACTIVE);
        bank.setCreated_at(LocalDateTime.now());
        bank.setUpdated_at(LocalDateTime.now());
        return bankRepository.save(bank);
    }

    private Account createTestAccount(String accountNumber, Bank bank, String holderName, BigDecimal balance) {
        // Check if account already exists
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            Account existing = accountRepository.findByAccountNumber(accountNumber);
            System.out.println("Account '" + accountNumber + "' already exists, using existing one.");
            return existing;
        }

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBank(bank);
        account.setAccountHolderName(holderName);
        account.setAccountType(AccountTypes.CHECKING);
        account.setBalance(balance);
        account.setCurrency(CurrencyEnum.USD);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());
        return accountRepository.save(account);
    }
}
