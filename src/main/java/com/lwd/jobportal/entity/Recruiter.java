package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "recruiter_profiles",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
    },
    indexes = {
        @Index(name = "idx_recruiter_company", columnList = "company_id"),
        @Index(name = "idx_recruiter_location", columnList = "location")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recruiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to user account
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Recruiter works for a company
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    private String designation;

    private Integer experience;

    private String location;

    private String phone;

    private String linkedinUrl;

    @Column(length = 1000)
    private String about;

    @Column(name = "profile_completion")
    private Integer profileCompletion = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
