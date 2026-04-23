package com.lwd.jobportal.pricing;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
