package com.example.tokengate.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RateLimitResponse {
    private final boolean allowed;
    private final long remainingTokens;

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemainingTokens() {
        return remainingTokens;
    }
}
