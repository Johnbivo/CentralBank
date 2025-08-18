package com.bivolaris.centralbank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {


    private Global global = new Global();
    private Auth auth = new Auth();
    private Transaction transaction = new Transaction();
    private Account account = new Account();
    private Fraud fraud = new Fraud();
    private Admin admin = new Admin();

    @Data
    public static class Global {
        private boolean enabled = true;
        private int requestsPerMinute = 100;
        private int requestsPerHour = 1000;
        private Duration windowSize = Duration.ofMinutes(1);
        private boolean skipInternalRequests = true;
    }

    @Data
    public static class Auth {
        private int loginAttemptsPerMinute = 5;
        private int loginAttemptsPerHour = 20;
        private int registrationAttemptsPerHour = 3;
        private int tokenRefreshPerMinute = 10;
        private int bankTokenRequestsPerMinute = 10;
    }

    @Data
    public static class Transaction {
        private int transactionsPerMinute = 10;
        private int transactionsPerHour = 100;
    }

    @Data
    public static class Account {
        private int accountCreationPerHour = 5;
        private int accountLookupPerMinute = 20;
    }

    @Data
    public static class Fraud {
        private int fraudReviewsPerMinute = 30;
        private int fraudQueryPerMinute = 50;
    }

    @Data
    public static class Admin {
        private int adminActionsPerMinute = 50;
        private int auditAccessPerMinute = 20;
    }
}
