package com.ratelimiter;

public class TokenBucket {
    private final long capacity;
    private final long refillRate; // tokens per second
    private long tokens;
    private long lastRefillTimestamp;

    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume(long tokensToConsume) {
        refill();
        if (tokens >= tokensToConsume) {
            tokens -= tokensToConsume;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTimestamp;
        // Refill rate is in tokens per second.
        // tokensToAdd = (elapsedTime / 1000.0) * refillRate
        // Ideally, we want to add tokens as integer amount.
        // If elapsedTime is small, tokensToAdd might be 0, which is fine for small intervals.
        long tokensToAdd = (elapsedTime * refillRate) / 1000;
        
        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}
