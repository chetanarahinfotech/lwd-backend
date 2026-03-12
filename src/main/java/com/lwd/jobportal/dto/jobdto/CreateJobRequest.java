package com.lwd.jobportal.dto.jobdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;

@Data
public class CreateJobRequest {
    @NotBlank
    private String title;

    private String description;
    private String location;
    private String industry;
    
    private Double minSalary;
    private Double maxSalary;

    private String roleCategory;
    private String department;
    private String workplaceType;

    private String education;
    private String skills;
    private String genderPreference;
    private Integer ageLimit;

    private String responsibilities;
    private String requirements;
    private String benefits;


    private Integer minExperience;   // new
    private Integer maxExperience;   // new
    private JobType jobType;         // new
    
    // ================= LWD SPECIFIC =================
    private NoticeStatus noticePreference;   // SERVING_NOTICE, IMMEDIATE_JOINER, etc.
    private Integer maxNoticePeriod;         // in days
    private Boolean lwdPreferred;            // true/false
    
    // ================= APPLICATION SOURCE =================
    private ApplicationSource applicationSource;      // PORTAL or EXTERNAL
    private String externalApplicationUrl;           // required if EXTERNAL
}
