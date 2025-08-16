package com.bivolaris.centralbank.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "fraudcases")
public class Fraudcase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private Employee reviewedBy;


    @Column(name = "reason")
    private String reason;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;



    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FraudStatus status;


    @Column(name = "flagged_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime flaggedAt;


    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

}