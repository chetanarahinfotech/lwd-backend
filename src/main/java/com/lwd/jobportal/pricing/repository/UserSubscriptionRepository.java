package com.lwd.jobportal.pricing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lwd.jobportal.pricing.entity.UserSubscription;
import com.lwd.jobportal.pricing.enums.SubscriptionStatus;

public interface UserSubscriptionRepository
        extends JpaRepository<UserSubscription, Long> {

    // 🔥 Get active subscription
    Optional<UserSubscription> findByUserIdAndStatus(
            Long userId,
            SubscriptionStatus status
    );

    // ⚡ Optimized fetch (with plan)
    @Query("""
        SELECT us FROM UserSubscription us
        JOIN FETCH us.plan p
        WHERE us.userId = :userId
        AND us.status = 'ACTIVE'
    """)
    Optional<UserSubscription> findActiveWithPlan(Long userId);
}
