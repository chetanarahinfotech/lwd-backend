package com.lwd.jobportal.authservice;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EmailRateLimiterService {

    // Cache: email -> last request timestamp
    private final Cache<String, Long> emailCache;

    public EmailRateLimiterService() {

        this.emailCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1)) // auto remove after 1 minute
                .maximumSize(10000) // protect memory
                .build();
    }

    /**
     * Check if email request is allowed
     */
    public boolean isAllowed(String email) {

        Long lastRequestTime = emailCache.getIfPresent(email);

        if (lastRequestTime != null) {
            return false; // request too soon
        }

        emailCache.put(email, System.currentTimeMillis());
        return true;
    }
}
