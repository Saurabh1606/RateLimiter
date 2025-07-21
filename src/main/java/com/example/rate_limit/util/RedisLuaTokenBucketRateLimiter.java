package com.example.rate_limit.util;

import ch.qos.logback.classic.Logger;
import com.example.rate_limit.dto.RateLimitStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class RedisLuaTokenBucketRateLimiter {
    private static final int DEFAULT_CAPACITY = 100;
    private static final int DEFAULT_REFILL_RATE = 100;
    private static final int SCRIPT_CACHE_TTL = 300;

    private final RedisScript<Long> script;
    private final StringRedisTemplate redisTemplate;

    public RedisLuaTokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = loadScript();
    }

    private RedisScript<Long> loadScript() {
        try {
            return RedisScript.of(
                    new ClassPathResource("scripts/token_bucket.lua"),
                    Long.class
            );
        } catch (Exception e) {

            throw new IllegalStateException("Could not initialize rate limiter", e);
        }
    }

    public boolean allowRequest(String userId) {
        return allowRequest(userId, DEFAULT_CAPACITY, DEFAULT_REFILL_RATE);
    }

    public boolean allowRequest(String userId, int capacity, int refillRate) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }

        try {
            String key = "rate_limit:token_bucket:" + userId;
            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(capacity),
                    String.valueOf(refillRate)
            );

            boolean allowed = result != null && result == 1L;


            return allowed;

        } catch (Exception e) {
            // Fail open - allow request if Redis is down
            return true;
        }
    }

    public RateLimitStatus getRateLimitStatus(String userId) {
        try {
            String key = "rate_limit:token_bucket:" + userId;
            List<Object> values = redisTemplate.opsForHash()
                    .multiGet(key, Arrays.asList("tokens", "last_refill"));

            String tokensStr = values.get(0).toString();
            String lastRefillStr = values.get(1).toString();

            int remainingTokens = tokensStr != null ?
                    Integer.parseInt(tokensStr) : DEFAULT_CAPACITY;
            long lastRefill = lastRefillStr != null ?
                    Long.parseLong(lastRefillStr) : System.currentTimeMillis();

            return new RateLimitStatus(remainingTokens, lastRefill, DEFAULT_CAPACITY);

        } catch (Exception e) {

            return new RateLimitStatus(DEFAULT_CAPACITY, System.currentTimeMillis(), DEFAULT_CAPACITY);
        }
    }


}