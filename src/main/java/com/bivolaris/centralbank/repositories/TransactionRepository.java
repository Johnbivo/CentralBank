package com.bivolaris.centralbank.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.bivolaris.centralbank.entities.Transaction;
import com.bivolaris.centralbank.entities.Account;
import com.bivolaris.centralbank.entities.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT fa as fromAccount, ta as toAccount, fb as fromBank, tb as toBank " +
            "FROM Account fa, Account ta, Bank fb, Bank tb " +
            "WHERE fa.accountNumber = :fromAccountNumber " +
            "AND ta.accountNumber = :toAccountNumber " +
            "AND fb.name = :fromBankName " +
            "AND tb.name = :toBankName")
    Optional<TransactionData> findTransactionData(
            @Param("fromAccountNumber") String fromAccountNumber,
            @Param("toAccountNumber") String toAccountNumber,
            @Param("fromBankName") String fromBankName,
            @Param("toBankName") String toBankName
    );

    // Fraud detection queries
    List<Transaction> findByFromAccountAndInitiatedAtAfterOrderByInitiatedAtDesc(
            Account fromAccount, LocalDateTime initiatedAt);

    long countByFromAccountAndInitiatedAtAfter(Account fromAccount, LocalDateTime initiatedAt);

    List<Transaction> findByFromAccountAndInitiatedAtAfterAndStatusIn(
            Account fromAccount, LocalDateTime initiatedAt, List<TransactionStatus> statuses);

    long countByFromAccountAndToAccountAndInitiatedAtAfter(
            Account fromAccount, Account toAccount, LocalDateTime initiatedAt);



    // Find transactions that are PENDING and were recently updated (likely cleared from fraud)
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.updatedAt > :since")
    List<Transaction> findPendingTransactionsUpdatedSince(@Param("since") LocalDateTime since);


    // Find recently updated pending transactions (last 5 minutes)
    default List<Transaction> findPendingTransactionsUpdatedRecently() {
        return findPendingTransactionsUpdatedSince(LocalDateTime.now().minusMinutes(5));
    }

}
