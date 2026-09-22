package com.example.tokengate.controller;

import com.example.tokengate.dto.RateLimitResponse;
import com.example.tokengate.service.TokenBucketRateLimiter;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/ratelimit")
@AllArgsConstructor
public class RateLimitController {
    private final TokenBucketRateLimiter rateLimiter;

    @PostMapping("/check")
    public RateLimitResponse check(@RequestParam String clientId) {
        return rateLimiter.tryConsume(clientId);
    }
}

//curl -X POST "http://localhost:8080/api/v1/ratelimit/check?clientId=user123"
