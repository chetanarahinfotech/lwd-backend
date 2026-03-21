package com.lwd.jobportal.pricing.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lwd.jobportal.pricing.entity.UserUsage;

public interface UserUsageRepository
        extends JpaRepository<UserUsage, Long> {

    Optional<UserUsage> findByUserIdAndFeatureCode(
            Long userId,
            String featureCode
    );

    // ⚡ Find usages that need reset
    @Query("""
        SELECT u FROM UserUsage u
        WHERE u.lastReset < :threshold
    """)
    List<UserUsage> findAllForReset(LocalDateTime threshold);

    // 🔥 Bulk reset (FAST)
    @Modifying
    @Query("""
        UPDATE UserUsage u
        SET u.usedCount = 0,
            u.lastReset = :now
        WHERE u.lastReset < :threshold
    """)
    int resetUsage(LocalDateTime threshold, LocalDateTime now);

    @Modifying
    @Query("""
        UPDATE UserUsage u
        SET u.usedCount = u.usedCount + 1
        WHERE u.userId = :userId
        AND u.featureCode = :featureCode
        AND (:limit IS NULL OR u.usedCount < :limit)
    """)
    int incrementIfAllowed(Long userId,
                           String featureCode,
                           Integer limit);

}
