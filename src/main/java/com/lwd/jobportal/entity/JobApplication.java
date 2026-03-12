package com.lwd.jobportal.entity;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.ApplicationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "job_applications",
    indexes = {

        // 🔹 Fast check: has user already applied for this job?
        @Index(
            name = "idx_job_jobseeker",
            columnList = "job_id, job_seeker_id",
            unique = true
        ),

        // 🔹 Recruiter / Company Admin: applications per job
        @Index(
            name = "idx_job_id",
            columnList = "job_id"
        ),

        // 🔹 Job Seeker: my applications
        @Index(
            name = "idx_job_seeker_id",
            columnList = "job_seeker_id"
        ),

        // 🔹 Admin filtering by status
        @Index(
            name = "idx_application_status",
            columnList = "status"
        ),

        // 🔹 Sorting & reports (recent applications)
        @Index(
            name = "idx_applied_at",
            columnList = "applied_at"
        )
    }
)
@NamedEntityGraph(
	    name = "JobApplication.full",
	    attributeNodes = {
	        @NamedAttributeNode(value = "job", subgraph = "job-subgraph"),
	        @NamedAttributeNode("jobSeeker")
	    },
	    subgraphs = {
	        @NamedSubgraph(
	            name = "job-subgraph",
	            attributeNodes = {
	                @NamedAttributeNode("company")
	            }
	        )
	    }
	)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Job reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // 🔹 Job Seeker reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private User jobSeeker;

    // 🔹 PORTAL or EXTERNAL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationSource applicationSource;

    // 🔹 External career page URL (if EXTERNAL)
    @Column(length = 500)
    private String externalApplyUrl;

    // Applicant details
    private String fullName;
    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private String resumeUrl;

    // 🔹 Application status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    // 🔹 Audit fields
    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
