package com.lwd.jobportal.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserActivityService {

    private final Cache<Long, Long> activeUsersCache;

    public UserActivityService() {

        activeUsersCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5)) // realtime activity window
                .maximumSize(100000)
                .build();
    }

    // update user activity
    public void updateActivity(Long userId) {
        activeUsersCache.put(userId, System.currentTimeMillis());
    }

    // check if user is active
    public boolean isUserActive(Long userId) {
        return activeUsersCache.getIfPresent(userId) != null;
    }

    // get all active users
    public Map<Long, Long> getActiveUsers() {
        return activeUsersCache.asMap();
    }
}
