package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.ApplicationSource;
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
	    	@Index(name = "idx_jobs_lwd_preferred", columnList = "lwd_preferred"),
	    	@Index(name = "idx_jobs_role_category", columnList = "role_category"),
	    	@Index(name = "idx_jobs_department", columnList = "department"),
	    	@Index(name = "idx_jobs_workplace_type", columnList = "workplace_type"),
	    	@Index(name = "idx_jobs_min_salary", columnList = "min_salary"),
	    	@Index(name = "idx_jobs_max_salary", columnList = "max_salary")


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

 // ================= SALARY =================

    @Column(name = "min_salary")
    private Double minSalary;

    @Column(name = "max_salary")
    private Double maxSalary;


    // ================= JOB CLASSIFICATION =================

    @Column(name = "role_category")
    private String roleCategory;

    @Column
    private String department;

    @Column(name = "workplace_type")
    private String workplaceType;


    // ================= CANDIDATE DETAILS =================

    @Column
    private String education;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "gender_preference")
    private String genderPreference;

    @Column(name = "age_limit")
    private Integer ageLimit;


    // ================= JOB CONTENT =================

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;


    // ================= JOB TYPE =================
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType; // FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT

    // ================= EXPERIENCE =================
    @Column(name = "min_experience")
    @Min(0)
    private Integer minExperience; // 0 = fresher

    @Column(name = "max_experience")
    @Min(0)
    private Integer maxExperience;

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status; // OPEN CLOSE
    
    // Allowed sources at job posting time
    @Enumerated(EnumType.STRING)
    @Column(name = "application_source", nullable = false)
    private ApplicationSource applicationSource;  // PORTEL, EXTERNAL

    @Column(name = "external_application_url", columnDefinition = "TEXT")
    private String externalApplicationUrl; // Only required if EXTERNAL

    
    // ================= LWD SPECIFIC =================

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_preference")
    private NoticeStatus noticePreference;

    @Column(name = "max_notice_period")
    @Min(0)
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
    @Builder.Default
    private Long viewCount = 0L;
    
    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;




    // ================= AUDIT =================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
        
        if (viewCount == null) viewCount = 0L;
        if (applicationSource == null) applicationSource = ApplicationSource.PORTAL;
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
