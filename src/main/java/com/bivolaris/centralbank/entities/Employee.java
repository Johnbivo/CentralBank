package com.bivolaris.centralbank.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;



    @Column(name = "first_name")
    private String firstName;



    @Column(name = "last_name")
    private String lastName;


    @Column(name = "email")
    private String email;


    @Column(name = "phone_number")
    private String phoneNumber;



    @Column(name = "address")
    private String address;


    @Column(name = "city")
    private String city;

    @Column(name = "profession")
    private String profession;


    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private Instant createdAt;


    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Instant updatedAt;

    @OneToMany
    @JoinColumn(name = "employee_id")
    private Set<Auditlog> auditlogs = new LinkedHashSet<>();

    @OneToMany
    @JoinColumn(name = "reviewed_by")
    private Set<Fraudcase> fraudcases = new LinkedHashSet<>();

}