package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;

@Entity
@Table(
	    name = "jobs",
	    indexes = {

	        // Latest jobs (pagination & feed)
	    	@Index(
	    		    name = "idx_jobs_deleted_status_created_at",
	   			    columnList = "deleted, status, created_at"
	   			)
,

	        // AUTOCOMPLETE & SEARCH (single-column indexes)
	        @Index(name = "idx_jobs_title", columnList = "title"),
	        @Index(name = "idx_jobs_location", columnList = "location"),
	        @Index(name = "idx_jobs_industry", columnList = "industry"),
	        @Index(name = "idx_jobs_deleted", columnList = "deleted"),


	        // Filters
	        @Index(name = "idx_jobs_job_type", columnList = "job_type"),
	        @Index(name = "idx_jobs_min_exp", columnList = "min_experience"),
	    	
	        // LWD SPECIFIC 
	    	@Index(name = "idx_jobs_notice_preference", columnList = "notice_preference"),
	    	@Index(name = "idx_jobs_max_notice_period", columnList = "max_notice_period"),
	    	@Index(name = "idx_jobs_lwd_preferred", columnList = "lwd_preferred")

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
    
 // ================= LWD SPECIFIC =================

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_preference")
    private NoticeStatus noticePreference;

    @Column(name = "max_notice_period")
    private Integer maxNoticePeriod; // in days

    @Column(nullable = false)
    @Builder.Default
    private Boolean lwdPreferred = false;



    // ================= RELATIONS =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private Long viewCount = 0L;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;



    // ================= AUDIT =================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime deletedAt;

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
    
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.status = JobStatus.CLOSED;
    }


}
