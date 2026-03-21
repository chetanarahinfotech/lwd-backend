package com.lwd.jobportal.pricing.entity;

import java.time.LocalDateTime;

import com.lwd.jobportal.pricing.enums.PlanName;
import com.lwd.jobportal.pricing.enums.PlanType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "plans",
    indexes = {
        @Index(name = "idx_plan_name", columnList = "name"),
        @Index(name = "idx_plan_type", columnList = "type"),
        @Index(name = "idx_plan_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 FIXED (Enum instead of String)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanName name; // FREE, PREMIUM, BASIC, STANDARD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType type; // CANDIDATE / RECRUITER

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 🔁 Auto set timestamps
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
