package com.bivolaris.centralbank.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;


    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;


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

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "transaction")
    private Fraudcase fraudcase;

}