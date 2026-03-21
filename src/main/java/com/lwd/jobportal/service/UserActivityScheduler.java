package com.lwd.jobportal.service;

import com.lwd.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserActivityScheduler {

    private final UserActivityService userActivityService;
    private final UserRepository userRepository;

    // Runs every 5 hours
    @Scheduled(fixedRate = 5 * 60 * 60 * 1000)
    public void updateLastActiveUsers() {

        Map<Long, Long> activeUsers = userActivityService.getActiveUsers();

        activeUsers.forEach((userId, timestamp) -> {

            LocalDateTime lastActive =
                    LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC);

            userRepository.findById(userId).ifPresent(user -> {
                user.setLastActiveAt(lastActive);
                userRepository.save(user);
            });
        });
    }
}
