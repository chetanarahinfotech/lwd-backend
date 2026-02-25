package com.lwd.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

import com.lwd.jobportal.enums.NoticeStatus;



@Entity
@Table(
	    name = "job_seeker_profiles",
	    uniqueConstraints = {
	        @UniqueConstraint(columnNames = "user_id")
	    },
	    indexes = {

	        // 🔹 Basic filters
	        @Index(name = "idx_js_experience", columnList = "total_experience"),
	        @Index(name = "idx_js_current_location", columnList = "current_location"),

	        // 🔹 Composite indexes (Most Important)
	        @Index(
	            name = "idx_location_experience",
	            columnList = "current_location, total_experience"
	        ),
	        @Index(
	            name = "idx_experience_ctc",
	            columnList = "total_experience, expected_ctc"
	        ),
	        @Index(
	            name = "idx_location_experience_ctc",
	            columnList = "current_location, total_experience, expected_ctc"
	        ),

	        // 🔹 Fast joiner search
	        @Index(
	            name = "idx_immediate_notice",
	            columnList = "immediate_joiner, notice_period"
	        ),

	        // 🔹 Availability filter
	        @Index(
	            name = "idx_available_from",
	            columnList = "available_from"
	        )
	    }
	)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeeker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private NoticeStatus noticeStatus;

    private Boolean isServingNotice;

    private LocalDate lastWorkingDay;

    private Integer noticePeriod;

    private LocalDate availableFrom;

    private Boolean immediateJoiner;

    private String currentCompany;

    @Column(name = "current_ctc")
    private Double currentCTC;

    @Column(name = "expected_ctc")
    private Double expectedCTC;

    @Column(name = "current_location")
    private String currentLocation;

    private String preferredLocation;

    @Column(name = "total_experience")
    private Integer totalExperience;

    @ManyToMany
    @JoinTable(
        name = "job_seeker_skills",
        joinColumns = @JoinColumn(name = "job_seeker_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id"),
        indexes = {
            @Index(name = "idx_js_skill_js_id", columnList = "job_seeker_id"),
            @Index(name = "idx_js_skill_skill_id", columnList = "skill_id"),
            @Index(name = "idx_js_skill_composite", columnList = "skill_id, job_seeker_id")
        }
    )
    private Set<Skill> skills;



    @Column(length = 1000)
    private String resumeUrl;
}
