package com.bivolaris.centralbank.controllers;

import com.bivolaris.centralbank.entities.CurrencyEnum;
import com.bivolaris.centralbank.services.AuditService;
import com.bivolaris.centralbank.services.CurrencyExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/currency")
@RequiredArgsConstructor
public class CurrencyController {
    
    private final CurrencyExchangeService currencyExchangeService;
    private final AuditService auditService;
    

    @GetMapping("/rate")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @RequestParam String from,
            @RequestParam String to) {
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "EXCHANGE_RATE_ACCESSED: " + from + "_" + to);
        }
        
        try {
            CurrencyEnum fromCurrency = CurrencyEnum.valueOf(from.toUpperCase());
            CurrencyEnum toCurrency = CurrencyEnum.valueOf(to.toUpperCase());
            
            BigDecimal rate = currencyExchangeService.getExchangeRate(fromCurrency, toCurrency);
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", from.toUpperCase());
            response.put("to", to.toUpperCase());
            response.put("rate", rate);
            response.put("lastUpdated", currencyExchangeService.getLastUpdateDate());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertCurrency(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "CURRENCY_CONVERSION: " + amount + " " + from + " to " + to);
        }
        
        try {
            CurrencyEnum fromCurrency = CurrencyEnum.valueOf(from.toUpperCase());
            CurrencyEnum toCurrency = CurrencyEnum.valueOf(to.toUpperCase());
            
            BigDecimal convertedAmount = currencyExchangeService.convertCurrency(amount, fromCurrency, toCurrency);
            BigDecimal rate = currencyExchangeService.getExchangeRate(fromCurrency, toCurrency);
            
            Map<String, Object> response = new HashMap<>();
            response.put("originalAmount", amount);
            response.put("fromCurrency", from.toUpperCase());
            response.put("convertedAmount", convertedAmount);
            response.put("toCurrency", to.toUpperCase());
            response.put("exchangeRate", rate);
            response.put("lastUpdated", currencyExchangeService.getLastUpdateDate());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    

    @GetMapping("/rates")
    public ResponseEntity<Map<String, Object>> getAllExchangeRates() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "ALL_EXCHANGE_RATES_ACCESSED");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("rates", currencyExchangeService.getAllExchangeRates());
        response.put("lastUpdated", currencyExchangeService.getLastUpdateDate());
        response.put("supportedCurrencies", new String[]{"USD", "EUR", "GBP"});
        
        return ResponseEntity.ok(response);
    }
    

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshExchangeRates() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long authId) {
            auditService.logEmployeeAction(authId, "MANUAL_EXCHANGE_RATE_REFRESH");
        }
        
        try {
            currencyExchangeService.fetchDailyExchangeRates();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exchange rates refreshed successfully");
            response.put("lastUpdated", currencyExchangeService.getLastUpdateDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to refresh exchange rates");
            response.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
