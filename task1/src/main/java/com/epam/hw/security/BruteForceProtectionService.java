package com.epam.hw.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    private final int MAX_ATTEMPTS = 3;
    private final long LOCK_TIME = 5 * 60 * 1000;

    public boolean isBlocked(String username) {
        AttemptInfo info = attempts.get(username);
        if (info == null) return false;

        if (info.attempts >= MAX_ATTEMPTS) {
            return (System.currentTimeMillis() - info.lastAttempt) < LOCK_TIME;
        }
        return false;
    }

    public void recordFailedAttempt(String username) {
        AttemptInfo info = attempts.computeIfAbsent(username, k -> new AttemptInfo());

        if (System.currentTimeMillis() - info.lastAttempt > LOCK_TIME) {
            info.attempts = 0;
        }

        info.attempts++;
        info.lastAttempt = System.currentTimeMillis();
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }

    private static class AttemptInfo {
        int attempts = 0;
        long lastAttempt = 0;
    }
}