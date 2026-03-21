package com.lwd.jobportal.pricing.entity;

import java.time.LocalDateTime;

import com.lwd.jobportal.pricing.enums.ActionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "subscription_history",
    indexes = {
        @Index(name = "idx_history_user", columnList = "user_id"),
        @Index(name = "idx_history_action", columnList = "action"),
        @Index(name = "idx_history_date", columnList = "changed_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👤 User reference
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 📦 Old Plan
    @Column(name = "old_plan_id")
    private Long oldPlanId;

    // 📦 New Plan
    @Column(name = "new_plan_id")
    private Long newPlanId;

    // 🔄 Action performed
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action; 
    // UPGRADE / DOWNGRADE / EXPIRE / RENEW

    // 💳 Payment info
    @Column(name = "amount_paid")
    private Double amountPaid;

    // 🕒 Timestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    // 🔁 Auto set timestamp
    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
