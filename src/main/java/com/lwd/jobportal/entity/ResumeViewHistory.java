package com.lwd.jobportal.entity;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "resume_view_history",
    indexes = {
        @Index(name = "idx_resume_view_resume_id", columnList = "resume_id"),
        @Index(name = "idx_resume_view_viewer_id", columnList = "viewer_id"),
        @Index(name = "idx_resume_viewed_at", columnList = "viewed_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_resume_view_resume_viewer_source",
            columnNames = {"resume_id", "viewer_id", "view_source"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "viewer_id", nullable = false)
    private Long viewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "viewer_role", 
    		nullable = false, 
    		length = 30,
    		columnDefinition = "ENUM('ADMIN','JOB_SEEKER','RECRUITER','RECRUITER_ADMIN','SUPER_ADMIN')"
    )
    private Role viewerRole;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "view_source", length = 50)
    private String viewSource; // PROFILE, APPLICATION, SEARCH, ADMIN

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
}