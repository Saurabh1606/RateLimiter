package com.example.rate_limit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public  class RateLimitStatus {
    private int remainingTokens;
    private long lastRefillTimestamp;
    private int capacity;

    public RateLimitStatus(int remainingTokens, long lastRefillTimestamp, int capacity) {
        this.remainingTokens = remainingTokens;
        this.lastRefillTimestamp = lastRefillTimestamp;
        this.capacity = capacity;
    }

    public int getRemainingTokens() {
        return remainingTokens;
    }

    public void setRemainingTokens(int remainingTokens) {
        this.remainingTokens = remainingTokens;
    }

    public long getLastRefillTimestamp() {
        return lastRefillTimestamp;
    }

    public void setLastRefillTimestamp(long lastRefillTimestamp) {
        this.lastRefillTimestamp = lastRefillTimestamp;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
