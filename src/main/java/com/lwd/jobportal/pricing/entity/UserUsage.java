package com.lwd.jobportal.pricing.entity;

import java.time.LocalDateTime;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_usage",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_feature", columnNames = {"user_id", "feature_code"})
    },
    indexes = {
        @Index(name = "idx_user_usage_user", columnList = "user_id"),
        @Index(name = "idx_user_usage_feature", columnList = "feature_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 🔥 Use Enum instead of String (IMPORTANT)
    @Column(name = "feature_code", nullable = false)
    private String featureCode;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "last_reset")
    private LocalDateTime lastReset;

    // ✅ Auto set default values
    @PrePersist
    public void prePersist() {
        if (usedCount == null) {
            usedCount = 0;
        }
        if (lastReset == null) {
            lastReset = LocalDateTime.now();
        }
    }
}
