package com.lwd.jobportal.pricing.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lwd.jobportal.pricing.entity.SubscriptionHistory;
import com.lwd.jobportal.pricing.enums.ActionType;

public interface SubscriptionHistoryRepository
        extends JpaRepository<SubscriptionHistory, Long> {

    List<SubscriptionHistory> findByUserId(Long userId);

    // 📊 Revenue query
    @Query("""
        SELECT SUM(h.amountPaid)
        FROM SubscriptionHistory h
        WHERE h.action = :action
        AND h.changedAt BETWEEN :start AND :end
    """)
    Double getRevenue(
            ActionType action,
            LocalDateTime start,
            LocalDateTime end
    );
}
