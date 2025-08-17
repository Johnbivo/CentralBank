package com.bivolaris.centralbank.services;


import com.bivolaris.centralbank.config.JwtConfig;
import com.bivolaris.centralbank.entities.Auth;
import com.bivolaris.centralbank.entities.AuthRole;
import com.bivolaris.centralbank.repositories.AuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

}
