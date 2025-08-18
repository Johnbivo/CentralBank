package com.bivolaris.centralbank.filters;


import com.bivolaris.centralbank.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = authHeader.replace("Bearer ", "");
        


        var jwt = jwtService.parseToken(token);
        if (jwt != null && !jwt.isExpired()) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    jwt.getUserAuthId(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getAuthRole()))
            );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }
        

        var bankClaims = jwtService.parseBankToken(token);
        if (bankClaims != null && !bankClaims.getExpiration().before(new java.util.Date())) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    bankClaims.get("bankId", String.class),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_BANK"))
            );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        // Neither token type worked
        filterChain.doFilter(request, response);
    }
}
