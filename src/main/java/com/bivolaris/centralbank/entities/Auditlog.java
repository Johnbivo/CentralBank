package com.bivolaris.centralbank.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditlogs")
@Getter
@Setter
@NoArgsConstructor
public class Auditlog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "action")
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "action_at")
    private LocalDateTime actionAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AuditStatus status;

    // Constructor for creating audit logs
    public Auditlog(Employee employee, Bank bank, String action, String ipAddress) {
        this.employee = employee;
        this.bank = bank;
        this.action = action;
        this.ipAddress = ipAddress;
        this.actionAt = LocalDateTime.now();
        this.status = AuditStatus.UNREVIEWED;
    }

    // Constructor for bank-only actions (when employee is null)
    public Auditlog(Bank bank, String action, String ipAddress) {
        this.bank = bank;
        this.action = action;
        this.ipAddress = ipAddress;
        this.actionAt = LocalDateTime.now();
        this.status = AuditStatus.UNREVIEWED;
    }
}