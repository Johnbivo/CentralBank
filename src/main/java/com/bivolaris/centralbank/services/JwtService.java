package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.config.JwtConfig;
import com.bivolaris.centralbank.entities.AuthRole;
import com.bivolaris.centralbank.entities.Bank;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {


    private final JwtConfig jwtConfig;



    public Jwt generateAccessToken(Long authId, AuthRole role) {
        return generateToken(authId, jwtConfig.getAccessTokenExpiration(), role);
    }

    public Jwt generateRefreshToken(Long authId, AuthRole role) {
        return generateToken(authId, jwtConfig.getRefreshTokenExpiration(), role);
    }



    private Jwt generateToken(Long authId,long tokenExpiration,  AuthRole role) {
        var claims = Jwts.claims()
                .subject(authId.toString())
                .add("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration * 1000L))
                .build();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }


    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, jwtConfig.getSecretKey());
        }catch (JwtException e) {
            return null;
        }
    }


    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }



    public String generateBankToken(Bank bank) {
        var claims = Jwts.claims()
                .subject(bank.getSwift())
                .add("bankId", bank.getId().toString())
                .add("bankName", bank.getName())
                .add("type", "BANK_AUTH")
                .issuer("CENTRAL_BANK")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300000L)) // 5 minutes
                .build();

        return Jwts.builder()
                .claims(claims)
                .signWith(jwtConfig.getInterBankSecretKey())
                .compact();
    }

    public Claims parseBankToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtConfig.getInterBankSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

}
