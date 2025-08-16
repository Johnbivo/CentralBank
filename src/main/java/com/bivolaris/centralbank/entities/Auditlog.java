package com.bivolaris.centralbank.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "auditlogs")
public class Auditlog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "action")
    private String action;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;


    @Column(name = "ip_address")
    private String ipAddress;


    @Column(name = "action_at")
    private LocalDateTime actionAt ;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AuditStatus status;

}