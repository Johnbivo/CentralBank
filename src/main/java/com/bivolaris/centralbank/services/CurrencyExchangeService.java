package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.ExchangeRateResponse;
import com.bivolaris.centralbank.entities.CurrencyEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyExchangeService {
    
    private final RestTemplate restTemplate;
    

    private final Map<String, BigDecimal> exchangeRates = new ConcurrentHashMap<>();
    private LocalDate lastUpdateDate;

    @PostConstruct
    public void initializeService() {
        log.info("Initializing CurrencyExchangeService...");
        try {
            fetchDailyExchangeRates();
            log.info("Currency exchange service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize currency exchange service: {}", e.getMessage());

            setFallbackRates();
        }
    }


    private void setFallbackRates() {
        log.warn("Setting fallback exchange rates");

        exchangeRates.put("EUR_USD", new BigDecimal("1.10"));
        exchangeRates.put("EUR_GBP", new BigDecimal("0.85"));
        exchangeRates.put("USD_EUR", new BigDecimal("0.91"));
        exchangeRates.put("USD_GBP", new BigDecimal("0.77"));
        exchangeRates.put("GBP_EUR", new BigDecimal("1.18"));
        exchangeRates.put("GBP_USD", new BigDecimal("1.30"));


        exchangeRates.put("EUR_EUR", BigDecimal.ONE);
        exchangeRates.put("USD_USD", BigDecimal.ONE);
        exchangeRates.put("GBP_GBP", BigDecimal.ONE);

        lastUpdateDate = LocalDate.now();
    }

    private static final String FRANKFURTER_API_URL = "https://api.frankfurter.dev/v1/latest";


    @Scheduled(cron = "0 0 9 * * *")
    public void fetchDailyExchangeRates() {
        try {
            log.info("Fetching daily exchange rates from Frankfurter API");


            fetchRatesForBase("EUR");
            fetchRatesForBase("USD");
            fetchRatesForBase("GBP");

            lastUpdateDate = LocalDate.now();
            log.info("Successfully updated exchange rates for {}", lastUpdateDate);

        } catch (Exception e) {
            log.error("Failed to fetch exchange rates: {}", e.getMessage(), e);
        }
    }


    public void initializeRatesIfNeeded() {
        if (exchangeRates.isEmpty() || lastUpdateDate == null || !lastUpdateDate.equals(LocalDate.now())) {
            fetchDailyExchangeRates();
        }
    }

    private void fetchRatesForBase(String baseCurrency) {
        try {
            String url = FRANKFURTER_API_URL + "?base=" + baseCurrency + "&symbols=USD,EUR,GBP";
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);

            if (response != null && response.getRates() != null) {

                for (Map.Entry<String, Double> entry : response.getRates().entrySet()) {
                    String key = baseCurrency + "_" + entry.getKey();
                    exchangeRates.put(key, BigDecimal.valueOf(entry.getValue()));
                }

                exchangeRates.put(baseCurrency + "_" + baseCurrency, BigDecimal.ONE);

                log.debug("Fetched {} rates for base currency {}", response.getRates().size(), baseCurrency);
            }
        } catch (Exception e) {
            log.error("Failed to fetch rates for base currency {}: {}", baseCurrency, e.getMessage());
        }
    }


    public BigDecimal convertCurrency(BigDecimal amount, CurrencyEnum fromCurrency, CurrencyEnum toCurrency) {

        initializeRatesIfNeeded();


        if (fromCurrency == toCurrency) {
            return amount;
        }

        String directKey = fromCurrency.name() + "_" + toCurrency.name();
        BigDecimal directRate = exchangeRates.get(directKey);

        if (directRate != null) {
            return amount.multiply(directRate).setScale(2, RoundingMode.HALF_UP);
        }

        String inverseKey = toCurrency.name() + "_" + fromCurrency.name();
        BigDecimal inverseRate = exchangeRates.get(inverseKey);

        if (inverseRate != null && inverseRate.compareTo(BigDecimal.ZERO) != 0) {
            return amount.divide(inverseRate, 6, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
        }


        return convertThroughBaseCurrency(amount, fromCurrency, toCurrency, CurrencyEnum.EUR);
    }


    private BigDecimal convertThroughBaseCurrency(BigDecimal amount, CurrencyEnum fromCurrency,
                                                 CurrencyEnum toCurrency, CurrencyEnum baseCurrency) {

        if (fromCurrency == baseCurrency) {

            String key = baseCurrency.name() + "_" + toCurrency.name();
            BigDecimal rate = exchangeRates.get(key);
            if (rate != null) {
                return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            }
        } else if (toCurrency == baseCurrency) {

            String key = fromCurrency.name() + "_" + baseCurrency.name();
            BigDecimal rate = exchangeRates.get(key);
            if (rate != null) {
                return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            }
        } else {

            BigDecimal toBase = convertCurrency(amount, fromCurrency, baseCurrency);
            return convertCurrency(toBase, baseCurrency, toCurrency);
        }

        log.error("No exchange rate found for {} to {} conversion", fromCurrency, toCurrency);
        throw new com.bivolaris.centralbank.exceptions.CurrencyConversionException(
            "Exchange rate not available for " + fromCurrency + " to " + toCurrency);
    }


    public BigDecimal getExchangeRate(CurrencyEnum fromCurrency, CurrencyEnum toCurrency) {
        initializeRatesIfNeeded();

        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE;
        }

        String key = fromCurrency.name() + "_" + toCurrency.name();
        BigDecimal rate = exchangeRates.get(key);

        if (rate != null) {
            return rate;
        }


        String inverseKey = toCurrency.name() + "_" + fromCurrency.name();
        BigDecimal inverseRate = exchangeRates.get(inverseKey);

        if (inverseRate != null && inverseRate.compareTo(BigDecimal.ZERO) != 0) {
            return BigDecimal.ONE.divide(inverseRate, 6, RoundingMode.HALF_UP);
        }

        throw new com.bivolaris.centralbank.exceptions.CurrencyConversionException(
            "Exchange rate not available for " + fromCurrency + " to " + toCurrency);
    }


    public Map<String, BigDecimal> getAllExchangeRates() {
        initializeRatesIfNeeded();
        return Map.copyOf(exchangeRates);
    }


    public LocalDate getLastUpdateDate() {
        return lastUpdateDate;
    }
}
