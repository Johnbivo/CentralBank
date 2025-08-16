package com.bivolaris.centralbank.entities;


import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "auth")
public class Auth {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name ="username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "agreed_to_terms")
    private boolean agreed_to_terms;

    @Column(name = "verified_email")
    private boolean verified_email;


    @Column(name = "tfa_enabled")
    private boolean tfa_enabled;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at" ,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updated_at;


    @Column(name = "last_login")
    private LocalDateTime last_login;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private authStatus status;




}
