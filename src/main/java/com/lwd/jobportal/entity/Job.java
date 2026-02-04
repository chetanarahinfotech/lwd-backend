package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;

@Entity
@Table(
    name = "jobs",
    indexes = {
        // Latest jobs
        @Index(
            name = "idx_jobs_status_created_at",
            columnList = "status, created_at"
        ),
        // Search optimization
        @Index(
            name = "idx_jobs_location_type_exp",
            columnList = "location, job_type, min_experience"
        ),
        @Index(name = "idx_jobs_industry", columnList = "industry")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String location;

    @Column
    private String industry;

    @Column
    private Double salary;

    // ================= JOB TYPE =================
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType; // FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT

    // ================= EXPERIENCE =================
    @Column(name = "min_experience")
    private Integer minExperience; // 0 = fresher

    @Column(name = "max_experience")
    private Integer maxExperience;

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    // ================= RELATIONS =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private Long viewCount = 0L;


    // ================= AUDIT =================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // ================= LIFECYCLE =================
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;

        if (status == null) status = JobStatus.OPEN;
        if (minExperience == null) minExperience = 0;
        
        if (viewCount == null) {
            viewCount = 0L;
        }
    }
    
   


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
