package com.example.tokengate.service;

import com.example.tokengate.dto.RateLimitResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class TokenBucketRateLimiter {
    private final StringRedisTemplate redisTemplate;
    private final long capacity;
    private final double refillRatePerSec;
    public TokenBucketRateLimiter(
            StringRedisTemplate redisTemplate,
            @Value("${ratelimit.capacity}") long capacity,
            @Value("${ratelimit.refill-rate-per-sec}") double refillRatePerSec) {
        this.redisTemplate = redisTemplate;
        this.capacity = capacity;
        this.refillRatePerSec = refillRatePerSec;
    }
    public RateLimitResponse tryConsume(String clientId) {
        String key = "ratelimit:"+ clientId;
        try {
            Map<Object,Object> bucket = redisTemplate.opsForHash().entries(key);
            long now = Instant.now().toEpochMilli();
            double tokens;
            long lastRefill;
            if (bucket.isEmpty()) {
                tokens = capacity;
                lastRefill = now;
            } else {
                tokens = Double.parseDouble((String) bucket.get("tokens"));
                lastRefill = Long.parseLong((String) bucket.get("lastRefill"));
            }
            double elapsedSeconds = (now-lastRefill)/1000.0;
            tokens = Math.min(capacity,tokens+elapsedSeconds*refillRatePerSec);

            boolean allowed = tokens>=1.0;
            if (allowed) {
                tokens -= 1.0;
            }
            redisTemplate.opsForHash().put(key, "tokens", String.valueOf(tokens));
            redisTemplate.opsForHash().put(key, "lastRefill", String.valueOf(now));
            redisTemplate.expire(key, Duration.ofMinutes(10)); // auto-cleanup idle clients
            return new RateLimitResponse(allowed, (long) tokens);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis is down. Failing open for client: {}", clientId);
            // Fallback: Allow the request but return -1 as remaining tokens to indicate degraded state
            return new RateLimitResponse(true, -1L);
        }
    }
}
