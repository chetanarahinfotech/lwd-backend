package com.lwd.jobportal.dto.jobdto;

import jakarta.validation.constraints.*;
import lombok.Data;

import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;

@Data
public class CreateJobRequest {

    // ================= BASIC =================
    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Industry is required")
    private String industry;

    // ================= SALARY =================
    @Positive(message = "Minimum salary must be positive")
    private Double minSalary;

    @Positive(message = "Maximum salary must be positive")
    private Double maxSalary;

    // ================= JOB DETAILS =================
    @NotBlank(message = "Role category is required")
    private String roleCategory;

    private String department;

    @NotBlank(message = "Workplace type is required (Remote/Hybrid/Onsite)")
    private String workplaceType;

    private String education;

    @Size(max = 1000, message = "Skills must not exceed 1000 characters")
    private String skills;

    private String genderPreference;

    @Min(value = 18, message = "Age limit must be at least 18")
    @Max(value = 65, message = "Age limit must be less than 65")
    private Integer ageLimit;

    @Size(max = 2000, message = "Responsibilities must not exceed 2000 characters")
    private String responsibilities;

    @Size(max = 2000, message = "Requirements must not exceed 2000 characters")
    private String requirements;

    @Size(max = 2000, message = "Benefits must not exceed 2000 characters")
    private String benefits;

    // ================= EXPERIENCE =================
    @Min(value = 0, message = "Minimum experience cannot be negative")
    private Integer minExperience;

    @Min(value = 0, message = "Maximum experience cannot be negative")
    private Integer maxExperience;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    // ================= LWD SPECIFIC =================
    private NoticeStatus noticePreference;

    @Min(value = 0, message = "Max notice period cannot be negative")
    private Integer maxNoticePeriod;

    private Boolean lwdPreferred;

    // ================= APPLICATION SOURCE =================
    @NotNull(message = "Application source is required")
    private ApplicationSource applicationSource;

    @Pattern(
        regexp = "^(https?://).*$",
        message = "External application URL must be a valid URL"
    )
    private String externalApplicationUrl;
}