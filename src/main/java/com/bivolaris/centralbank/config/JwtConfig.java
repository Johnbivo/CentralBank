package com.bivolaris.centralbank.config;


import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JwtConfig {

    private String secret;
    private String interBankSecret;
    private int accessTokenExpiration;
    private int refreshTokenExpiration;

    public SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public SecretKey getInterBankSecretKey(){
        return Keys.hmacShaKeyFor(interBankSecret.getBytes());
    }
}
