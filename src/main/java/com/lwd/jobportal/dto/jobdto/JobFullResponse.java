package com.lwd.jobportal.dto.jobdto;

import java.time.LocalDateTime;

import com.lwd.jobportal.dto.companydto.CompanySummaryDTO;
import com.lwd.jobportal.enums.ApplicationSource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobFullResponse {

    // ================= BASIC INFO =================
    private Long id;
    private String title;
    private String description;
    private String location;
    private String industry;

    // ================= SALARY =================
    private Double minSalary;
    private Double maxSalary;

    // ================= EXPERIENCE =================
    private Integer minExperience;
    private Integer maxExperience;

    // ================= JOB DETAILS =================
    private String jobType;
    private String roleCategory;
    private String department;
    private String workplaceType;

    // ================= CANDIDATE PREFERENCES =================
    private String education;
    private String skills;
    private String genderPreference;
    private Integer ageLimit;

    // ================= JOB CONTENT =================
    private String responsibilities;
    private String requirements;
    private String benefits;

    // ================= STATUS =================
    private String status;
    private Boolean deleted;

    // ================= APPLICATION DATA =================
    private ApplicationSource applicationSource;
    private String externalApplicationUrl;

    // ================= LWD FEATURES =================
    private String noticePreference;
    private Integer maxNoticePeriod;
    private Boolean lwdPreferred;

    // ================= META =================
    private String createdBy;
    private LocalDateTime createdAt;

    // ================= ANALYTICS =================
    private Long totalApplications;

    // ================= RELATIONS =================
    private CompanySummaryDTO company;

    // ================= RECOMMENDATION =================
    private Integer matchScore;

    // =====================================================
    // 🔥 PREMIUM FEATURE SECTION
    // =====================================================

    // Feature flags
    private Boolean canViewRecruiterDetails;
    private Boolean canMessageRecruiter;
    private Boolean premiumLocked;

    // Recruiter info (only if allowed)
    private Long recruiterId;
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterPhone;

    // Optional UX fields
    private String upgradeMessage;
}