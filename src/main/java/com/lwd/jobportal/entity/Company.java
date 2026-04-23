package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.CompanyStatus;

@Entity
@Table(
    name = "companies",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_company_name", columnNames = "company_name"),
        @UniqueConstraint(name = "uk_company_email", columnNames = "email")
    },
    indexes = {
        @Index(name = "idx_company_created_by", columnList = "created_by"),
        @Index(name = "idx_company_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Basic Info
    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String industry;

    @Column(length = 255)
    private String website;

    @Column(length = 500)
    private String logoUrl;

    // 🔹 Contact Info
    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    // 🔹 Address Info
    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    // 🔹 Business Info
    @Column(length = 100)
    private String companySize;   // e.g., 1-10, 11-50, 50-200

    @Column(length = 100)
    private String foundedYear;

    @Column(length = 100)
    private String companyType;   // e.g., Product, Service, Startup

    // 🔹 Status & Trust
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CompanyStatus status;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Boolean profileCompleted = false;

    // 🔹 Ownership & Approval
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    private LocalDateTime approvedAt;

    // 🔹 Analytics (for future trust engine)
    private Long totalJobsPosted = 0L;
    private Long totalHires = 0L;
    private Double rating; // avg rating (future feature)

    // 🔹 Audit
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 🔹 Lifecycle Hooks
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = CompanyStatus.PENDING;
        }

        if (this.verified == null) {
            this.verified = false;
        }

        if (this.profileCompleted == null) {
            this.profileCompleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}