package com.bivolaris.centralbank.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ExchangeRateResponse {
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("base")
    private String base;
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("rates")
    private Map<String, Double> rates;
}