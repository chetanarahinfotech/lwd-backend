package com.lwd.jobportal.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ApiRateLimiterService {

    private final Cache<String, Integer> requestCache;

    public ApiRateLimiterService() {
        requestCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(10000)
                .build();
    }

    public boolean isAllowed(String key) {

        Integer count = requestCache.getIfPresent(key);

        if (count == null) {
            requestCache.put(key, 1);
            return true;
        }

        if (count > 60) { // limit: 60 requests per minute
            return false;
        }

        requestCache.put(key, count + 1);
        return true;
    }
}
