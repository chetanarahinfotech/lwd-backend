package com.lwd.jobportal.pricing.entity;

import java.time.LocalDateTime;

import com.lwd.jobportal.pricing.enums.SubscriptionStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_subscriptions",
    indexes = {
        @Index(name = "idx_user_subscription_user", columnList = "user_id"),
        @Index(name = "idx_user_subscription_status", columnList = "status"),
        @Index(name = "idx_user_subscription_plan", columnList = "plan_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👤 User reference
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 📦 Plan
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // 📅 Subscription duration
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // 📊 Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    // 🕒 Audit fields
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 🔁 Auto timestamps
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
