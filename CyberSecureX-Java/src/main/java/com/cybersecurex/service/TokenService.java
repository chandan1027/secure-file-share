package com.cybersecurex.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private final Map<String, Instant> tokens = new ConcurrentHashMap<>();

    public String issueToken(String username) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, Instant.now().plus(TOKEN_TTL));
        return token;
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Instant expiry = tokens.get(token);
        if (expiry == null) {
            return false;
        }
        if (expiry.isBefore(Instant.now())) {
            tokens.remove(token);
            return false;
        }
        return true;
    }
}

