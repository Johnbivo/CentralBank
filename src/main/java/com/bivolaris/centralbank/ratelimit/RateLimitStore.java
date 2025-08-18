package com.bivolaris.centralbank.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitStore {

    private final ConcurrentHashMap<String, RateLimitEntry> store = new ConcurrentHashMap<>();


    public RateLimitResult checkAndIncrement(String key, int maxRequests, long windowSizeSeconds) {
        long currentWindow = getCurrentWindow(windowSizeSeconds);
        String windowKey = key + ":" + currentWindow;

        RateLimitEntry entry = store.computeIfAbsent(windowKey, k -> new RateLimitEntry(currentWindow, new AtomicInteger(0)));


        if (Math.random() < 0.01) {
            cleanupOldEntries(currentWindow, windowSizeSeconds);
        }

        int currentCount = entry.getCounter().incrementAndGet();
        boolean allowed = currentCount <= maxRequests;

        return new RateLimitResult(
            allowed,
            currentCount,
            maxRequests,
            getRemainingRequests(currentCount, maxRequests),
            getResetTime(currentWindow, windowSizeSeconds)
        );
    }


    public int getCurrentRequests(String key, long windowSizeSeconds) {
        long currentWindow = getCurrentWindow(windowSizeSeconds);
        String windowKey = key + ":" + currentWindow;
        
        RateLimitEntry entry = store.get(windowKey);
        return entry != null ? entry.getCounter().get() : 0;
    }

    private long getCurrentWindow(long windowSizeSeconds) {
        return Instant.now().getEpochSecond() / windowSizeSeconds;
    }


    private int getRemainingRequests(int currentCount, int maxRequests) {
        return Math.max(0, maxRequests - currentCount);
    }


    private long getResetTime(long currentWindow, long windowSizeSeconds) {
        return (currentWindow + 1) * windowSizeSeconds;
    }


    private void cleanupOldEntries(long currentWindow, long windowSizeSeconds) {
        long cutoffWindow = currentWindow - 2;
        
        store.entrySet().removeIf(entry -> {
            try {
                String[] parts = entry.getKey().split(":");
                if (parts.length >= 2) {
                    long entryWindow = Long.parseLong(parts[parts.length - 1]);
                    return entryWindow < cutoffWindow;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid window key format: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }


    public int getStoreSize() {
        return store.size();
    }


    public void clear() {
        store.clear();
    }


    private static class RateLimitEntry {
        private final long window;
        private final AtomicInteger counter;

        public RateLimitEntry(long window, AtomicInteger counter) {
            this.window = window;
            this.counter = counter;
        }



        public AtomicInteger getCounter() {
            return counter;
        }
    }
}
