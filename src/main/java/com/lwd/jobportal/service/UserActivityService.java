package com.lwd.jobportal.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    
    public Map<Long, Boolean> getActiveUsers(List<Long> userIds) {

        Map<Long, Long> activeUsers = activeUsersCache.asMap();

        return userIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        activeUsers::containsKey
                ));
    }
}
