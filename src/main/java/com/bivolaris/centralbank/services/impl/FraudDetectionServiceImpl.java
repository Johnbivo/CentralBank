package com.bivolaris.centralbank.services.impl;

import com.bivolaris.centralbank.entities.*;
import com.bivolaris.centralbank.repositories.FraudCaseRepository;
import com.bivolaris.centralbank.repositories.TransactionRepository;
import com.bivolaris.centralbank.services.FraudDetectionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllArgsConstructor
@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudCaseRepository fraudCaseRepository;
    private final TransactionRepository transactionRepository;

    // Fraud detection thresholds
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal UNUSUAL_AMOUNT_THRESHOLD = new BigDecimal("5000");
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final int MAX_TRANSACTIONS_PER_DAY = 50;
    private static final BigDecimal MAX_DAILY_AMOUNT = new BigDecimal("50000");


    public boolean detectFraud(Transaction transaction) {
        StringBuilder reasons = new StringBuilder();
        boolean isFraudulent = false;


        if (isHighAmountTransaction(transaction)) {
            reasons.append("High amount transaction (>").append(HIGH_AMOUNT_THRESHOLD).append("); ");
            isFraudulent = true;
        }


        if (isUnusualTransactionPattern(transaction)) {
            reasons.append("Unusual transaction pattern detected; ");
            isFraudulent = true;
        }


        if (isHighFrequencyFraud(transaction)) {
            reasons.append("High frequency transactions detected; ");
            isFraudulent = true;
        }


        if (isDailyLimitViolation(transaction)) {
            reasons.append("Daily transaction limit exceeded; ");
            isFraudulent = true;
        }


        if (isSuspiciousSameAccountTransfer(transaction)) {
            reasons.append("Suspicious same-account transfer pattern; ");
            isFraudulent = true;
        }


        if (isOffHoursTransaction(transaction)) {
            reasons.append("Transaction initiated during off-hours; ");
            isFraudulent = true;
        }

        if (isFraudulent) {
            flagTransactionForFraud(transaction, reasons.toString().trim());
        }

        return isFraudulent;
    }


    @Transactional
    public Fraudcase flagTransactionForFraud(Transaction transaction, String reason) {

        transaction.setStatus(TransactionStatus.FLAGGED_FOR_FRAUD);
        transactionRepository.save(transaction);


        Fraudcase fraudCase = new Fraudcase();
        fraudCase.setTransaction(transaction);
        fraudCase.setBank(transaction.getFromBank());
        fraudCase.setReason(reason);
        fraudCase.setStatus(FraudStatus.PENDING);
        fraudCase.setFlaggedAt(LocalDateTime.now());
        fraudCase.setUpdatedAt(LocalDateTime.now());

        return fraudCaseRepository.save(fraudCase);
    }

    /**
     * Checks if transaction amount is unusually high
     */
    private boolean isHighAmountTransaction(Transaction transaction) {
        return transaction.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0;
    }

    /**
     * Checks for unusual transaction patterns based on historical data
     */
    private boolean isUnusualTransactionPattern(Transaction transaction) {
        // Get recent transactions from the same account
        List<Transaction> recentTransactions = transactionRepository
                .findByFromAccountAndInitiatedAtAfterOrderByInitiatedAtDesc(
                        transaction.getFromAccount(),
                        LocalDateTime.now().minus(30, ChronoUnit.DAYS)
                );

        if (recentTransactions.isEmpty()) {
            return false;
        }

        // Calculate average transaction amount
        BigDecimal totalAmount = recentTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageAmount = totalAmount.divide(
                new BigDecimal(recentTransactions.size()), 
                2, 
                java.math.RoundingMode.HALF_UP
        );

        // Check if current transaction is significantly higher than average
        BigDecimal threshold = averageAmount.multiply(new BigDecimal("5"));
        return transaction.getAmount().compareTo(threshold) > 0 && 
               transaction.getAmount().compareTo(UNUSUAL_AMOUNT_THRESHOLD) > 0;
    }

    /**
     * Checks for high frequency transactions (velocity checks)
     */
    private boolean isHighFrequencyFraud(Transaction transaction) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        LocalDateTime oneDayAgo = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

        // Check transactions per hour
        long transactionsLastHour = transactionRepository
                .countByFromAccountAndInitiatedAtAfter(transaction.getFromAccount(), oneHourAgo);

        // Check transactions per day
        long transactionsLastDay = transactionRepository
                .countByFromAccountAndInitiatedAtAfter(transaction.getFromAccount(), oneDayAgo);

        return transactionsLastHour >= MAX_TRANSACTIONS_PER_HOUR || 
               transactionsLastDay >= MAX_TRANSACTIONS_PER_DAY;
    }

    /**
     * Checks if daily transaction limit is exceeded
     */
    private boolean isDailyLimitViolation(Transaction transaction) {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        
        List<Transaction> todaysTransactions = transactionRepository
                .findByFromAccountAndInitiatedAtAfterAndStatusIn(
                        transaction.getFromAccount(),
                        startOfDay,
                        List.of(TransactionStatus.COMPLETED, TransactionStatus.PENDING)
                );

        BigDecimal totalTodayAmount = todaysTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithCurrentTransaction = totalTodayAmount.add(transaction.getAmount());
        
        return totalWithCurrentTransaction.compareTo(MAX_DAILY_AMOUNT) > 0;
    }

    /**
     * Checks for suspicious same-account transfers that might indicate money laundering
     */
    private boolean isSuspiciousSameAccountTransfer(Transaction transaction) {
        // Check if it's the same account holder transferring to themselves frequently
        if (!transaction.getFromAccount().getAccountHolderName()
                .equals(transaction.getToAccount().getAccountHolderName())) {
            return false;
        }

        // Check for multiple same-account transfers in a short time
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        long sameAccountTransfers = transactionRepository
                .countByFromAccountAndToAccountAndInitiatedAtAfter(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        oneHourAgo
                );

        return sameAccountTransfers >= 3;
    }

    /**
     * Checks if transaction is initiated during off-hours (potential indicator)
     */
    private boolean isOffHoursTransaction(Transaction transaction) {
        int hour = transaction.getInitiatedAt().getHour();
        return hour >= 23 || hour <= 6;
    }

    /**
     * Reviews a fraud case (for admin/employee use)
     */
    @Transactional
    public boolean reviewFraudCase(String fraudCaseId, FraudStatus newStatus, Employee reviewedBy) {
        try {
            Fraudcase fraudCase = fraudCaseRepository.findById(java.util.UUID.fromString(fraudCaseId))
                    .orElse(null);
            
            if (fraudCase == null) {
                return false;
            }

            fraudCase.setStatus(newStatus);
            fraudCase.setReviewedBy(reviewedBy);
            fraudCase.setUpdatedAt(LocalDateTime.now());
            

            fraudCaseRepository.save(fraudCase);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Fraudcase> getPendingFraudCases() {
        return fraudCaseRepository.findByStatusOrderByFlaggedAtDesc(FraudStatus.PENDING);
    }


    public List<Fraudcase> getFraudCasesByStatus(FraudStatus status) {
        return fraudCaseRepository.findByStatusOrderByFlaggedAtDesc(status);
    }
}
