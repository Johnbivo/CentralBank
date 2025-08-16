package com.bivolaris.centralbank.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "from_account_id")
    private UUID fromAccountId;


    @Column(name = "to_account_id")
    private UUID toAccountId;


    @ManyToOne
    @JoinColumn(name = "from_bank_id")
    private Bank fromBank;


    @ManyToOne
    @JoinColumn(name = "to_bank_id")
    private Bank toBank;


    @Column(name = "amount")
    private BigDecimal amount;



    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;


    @Column(name = "message")
    private String message;


    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToOne(mappedBy = "transaction")
    private Fraudcase fraudcase;

}