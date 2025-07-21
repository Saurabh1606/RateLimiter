package com.example.rate_limit.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
public class RedisRateLimiter {

    private final String KEY_PREFIX = "rate_limit:";
    private final int WINDOW_SIZE = 60 * 1000; // 60 seconds
    private final int LIMIT = 100;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean isAllowed(String userId) {
        String key = KEY_PREFIX + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_SIZE;

        // Remove old entries
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // Count current requests
        Long currentCount = redisTemplate.opsForZSet().zCard(key);

        if (currentCount != null && currentCount >= LIMIT) {
            return false;
        }

        // Add current request
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        redisTemplate.expire(key, Duration.ofMinutes(2));

        return true;
    }
}
