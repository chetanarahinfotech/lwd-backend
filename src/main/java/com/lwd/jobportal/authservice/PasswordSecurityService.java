package com.lwd.jobportal.authservice;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class PasswordSecurityService {

    private static final int FORGOT_LIMIT = 3;
    private static final int RESET_LIMIT = 5;
    private static final int CHANGE_FAIL_LIMIT = 5;
    private static final int LOCK_MINUTES = 15;

    private final Cache<String, Integer> forgotAttempts = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    private final Cache<String, Integer> resetAttempts = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    private final Cache<String, Integer> changeFailures = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(10_000)
            .build();

    private final ConcurrentHashMap<String, LocalDateTime> lockedEmails = new ConcurrentHashMap<>();

    public void checkForgotAllowed(String email) {
        int count = forgotAttempts.get(email, key -> 0);
        if (count >= FORGOT_LIMIT) {
            throw new IllegalArgumentException("Too many forgot password requests. Try again later.");
        }
    }

    public void recordForgotAttempt(String email) {
        int count = forgotAttempts.get(email, key -> 0);
        forgotAttempts.put(email, count + 1);
    }

    public void checkResetAllowed(String email) {
        int count = resetAttempts.get(email, key -> 0);
        if (count >= RESET_LIMIT) {
            throw new IllegalArgumentException("Too many reset password attempts. Try again later.");
        }
    }

    public void recordResetAttempt(String email) {
        int count = resetAttempts.get(email, key -> 0);
        resetAttempts.put(email, count + 1);
    }

    public void checkChangeAllowed(String email) {
        LocalDateTime lockedUntil = lockedEmails.get(email);

        if (lockedUntil != null) {
            if (LocalDateTime.now().isBefore(lockedUntil)) {
                throw new IllegalArgumentException(
                        "Account temporarily locked due to too many failed password attempts. Try again later."
                );
            } else {
                lockedEmails.remove(email);
                changeFailures.invalidate(email);
            }
        }
    }

    public void recordChangeFailure(String email) {
        int count = changeFailures.get(email, key -> 0) + 1;
        changeFailures.put(email, count);

        if (count >= CHANGE_FAIL_LIMIT) {
            lockedEmails.put(email, LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        }
    }

    public void clearChangeFailures(String email) {
        changeFailures.invalidate(email);
        lockedEmails.remove(email);
    }
}