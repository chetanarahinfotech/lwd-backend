package com.lwd.jobportal.service;

import java.util.ArrayList;
import java.util.List;
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

    // Run every 1 hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void syncActiveUsers() {

        Map<Long, Long> activeUsers = activityService.getActiveUsers();

        if (activeUsers.isEmpty()) return;

        List<Long> userIds = new ArrayList<>(activeUsers.keySet());

        userRepository.updateUsersLastActive(userIds);
    }
}
