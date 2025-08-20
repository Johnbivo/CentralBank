package com.bivolaris.centralbank.repositories;

import com.bivolaris.centralbank.entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountRepositoryTests extends BaseRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void account_findByAccountNumber(){


        Bank testBank = new Bank();
        testBank.setName("Test Bank");
        testBank.setSwift("TESTBK01");
        testBank.setBase_api_endpoint("http://localhost:8080");
        testBank.setStatus(BankStatus.ACTIVE);
        testBank.setCreated_at(LocalDateTime.now());
        testBank.setUpdated_at(LocalDateTime.now());
        testBank = entityManager.persistAndFlush(testBank);

        Account testAccount = new Account();
        testAccount.setAccountNumber("ACC123456789");
        testAccount.setBank(testBank);
        testAccount.setAccountHolderName("John Doe");
        testAccount.setAccountType(AccountTypes.CHECKING);
        testAccount.setBalance(new BigDecimal("1500.00"));
        testAccount.setCurrency(CurrencyEnum.USD);
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setCreatedAt(Instant.now());
        testAccount.setUpdatedAt(Instant.now());
        entityManager.persistAndFlush(testAccount);



        String accountNumber = "ACC123456789";
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber);


        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(foundAccount.getAccountHolderName()).isEqualTo("John Doe");
        assertThat(foundAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(foundAccount.getCurrency()).isEqualTo(CurrencyEnum.USD);
        assertThat(foundAccount.getBank().getName()).isEqualTo("Test Bank");


        Account notFound = accountRepository.findByAccountNumber("NONEXISTENT");
        assertThat(notFound).isNull();
    }

    @Test
    public void test_existsByAccountNumber(){

        Bank testBank = new Bank();
        testBank.setName("Another Bank");
        testBank.setSwift("ANOTHBK01");
        testBank.setBase_api_endpoint("http://localhost:8081");
        testBank.setStatus(BankStatus.ACTIVE);
        testBank.setCreated_at(LocalDateTime.now());
        testBank.setUpdated_at(LocalDateTime.now());
        testBank = entityManager.persistAndFlush(testBank);

        Account testAccount = new Account();
        testAccount.setAccountNumber("ACC999888777");
        testAccount.setBank(testBank);
        testAccount.setAccountHolderName("Jane Smith");
        testAccount.setAccountType(AccountTypes.SAVINGS);
        testAccount.setBalance(new BigDecimal("3000.00"));
        testAccount.setCurrency(CurrencyEnum.EUR);
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setCreatedAt(Instant.now());
        testAccount.setUpdatedAt(Instant.now());
        entityManager.persistAndFlush(testAccount);


        boolean exists = accountRepository.existsByAccountNumber("ACC999888777");
        assertThat(exists).isTrue();

        boolean notExists = accountRepository.existsByAccountNumber("FAKE123");
        assertThat(notExists).isFalse();
    }
}
