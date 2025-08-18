package com.bivolaris.centralbank.entities;

import com.bivolaris.centralbank.dtos.CreateAccountRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {



    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;


    @Column(name = "account_number")
    private String accountNumber;


    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;


    @Column(name = "account_holder_name")
    private String accountHolderName;


    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountTypes accountType;


    @Column(name = "balance")
    private BigDecimal balance;



    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AccountStatus status;


    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany
    @JoinColumn(name = "from_account_id")
    private Set<Transaction> fromAccount = new LinkedHashSet<>();

    @OneToMany
    @JoinColumn(name = "to_account_id")
    private Set<Transaction> toAccount = new LinkedHashSet<>();



}