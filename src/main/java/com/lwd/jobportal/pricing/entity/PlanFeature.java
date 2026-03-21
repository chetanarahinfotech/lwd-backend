package com.lwd.jobportal.pricing.entity;

import java.time.LocalDateTime;

import com.lwd.jobportal.pricing.enums.LimitType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "plan_features",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_plan_feature",
            columnNames = {"plan_id", "feature_id"}
        )
    },
    indexes = {
        @Index(
                name = "idx_plan_feature_plan_feature",
                columnList = "plan_id, feature_id" // 🔥 COMPOSITE INDEX
            )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Plan
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // 🔗 Feature
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    // 🎯 Limit (NULL = unlimited)
    @Column(name = "limit_value")
    private Integer limitValue;

    // ✅ Feature enabled or not
    @Column(nullable = false)
    private Boolean enabled = true;

    // 📊 Limit type
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type")
    private LimitType limitType; // DAILY / MONTHLY / TOTAL

    // 🕒 Audit
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
