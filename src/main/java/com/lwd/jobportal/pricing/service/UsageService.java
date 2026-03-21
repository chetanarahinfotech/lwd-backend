package com.lwd.jobportal.pricing.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.exception.UsageLimitExceededException;
import com.lwd.jobportal.pricing.entity.UserUsage;
import com.lwd.jobportal.pricing.repository.UserUsageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsageService {

    private final UserUsageRepository usageRepo;

    /**
     * 🔍 Get or create usage record
     */
    @Transactional
    public UserUsage getOrCreate(Long userId, String featureCode) {

        return usageRepo.findByUserIdAndFeatureCode(userId, featureCode)
                .orElseGet(() -> usageRepo.save(
                        UserUsage.builder()
                                .userId(userId)
                                .featureCode(featureCode)
                                .usedCount(0)
                                .lastReset(LocalDateTime.now())
                                .build()
                ));
    }

    /**
     * 🔥 ATOMIC increment with limit check (NO RACE CONDITION)
     */
    @Transactional
    public void incrementWithLimit(Long userId,
                                   String featureCode,
                                   Integer limit) {

        // ensure record exists
        UserUsage usage = getOrCreate(userId, featureCode);

        // 🔁 reset before increment
        resetIfNeeded(usage);

        // ⚡ atomic DB update
        int updated = usageRepo.incrementIfAllowed(
                userId,
                featureCode,
                limit
        );

        if (updated == 0) {
        	throw new UsageLimitExceededException(
        		    "Daily limit reached. Upgrade your plan for more access."
        		);

        }
    }

    /**
     * 🔁 Reset usage (daily)
     */
    @Transactional
    public void resetIfNeeded(UserUsage usage) {

        LocalDateTime now = LocalDateTime.now();

        if (usage.getLastReset() == null ||
            usage.getLastReset().isBefore(now.minusDays(1))) {

            usage.setUsedCount(0);
            usage.setLastReset(now);

            usageRepo.save(usage); // ✅ persist fix
        }
    }
}
