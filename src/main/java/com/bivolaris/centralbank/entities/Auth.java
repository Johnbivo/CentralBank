package com.bivolaris.centralbank.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
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

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "employee_id")
    private Employee employee;


    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private AuthRole role;


    public static Auth createNewAuthForUser(String username, String password, boolean agreedToTerms) {
        Auth auth = new Auth();
        auth.setUsername(username);
        auth.setPassword(password);
        auth.setAgreed_to_terms(agreedToTerms);
        auth.setVerified_email(false);
        auth.setTfa_enabled(false);
        auth.setCreated_at(LocalDateTime.now());
        auth.setUpdated_at(LocalDateTime.now());
        auth.setStatus(authStatus.INACTIVE);
        auth.setRole(AuthRole.USER);
        return auth;
    }




}
