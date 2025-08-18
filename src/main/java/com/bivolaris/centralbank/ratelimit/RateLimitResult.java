package com.bivolaris.centralbank.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitResult {
    

    private boolean allowed;
    private int currentRequests;
    private int maxRequests;
    private int remainingRequests;
    private long resetTime;
    public boolean isRateLimitExceeded() {
        return !allowed;
    }
    public long getRetryAfterSeconds() {
        return Math.max(0, resetTime - (System.currentTimeMillis() / 1000));
    }
}
