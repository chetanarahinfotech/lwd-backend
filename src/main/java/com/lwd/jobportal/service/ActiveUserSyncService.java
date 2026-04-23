package com.lwd.jobportal.service;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ActiveUserSyncService {

    private final UserRepository userRepository;
    private final UserActivityService activityService;

    public ActiveUserSyncService(UserRepository userRepository,
                                 UserActivityService activityService) {
        this.userRepository = userRepository;
        this.activityService = activityService;
    }


    // every 5 minutes
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void syncActiveUsers() {

        Map<Long, Long> activeUsers = activityService.getActiveUsers();

        if (activeUsers.isEmpty()) {
            return;
        }

        userRepository.updateUsersLastActive(new ArrayList<>(activeUsers.keySet()));
    }
}
