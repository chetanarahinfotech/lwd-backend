package com.lwd.jobportal.jobapplication;

import java.time.LocalDateTime;

import com.lwd.jobportal.company.CompanySummaryDTO;
import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.job.JobSummaryDTO;
import com.lwd.jobportal.jobseeker.JobSeekerSummaryDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobApplicationResponse {

    private Long applicationId;

    // 🔹 Applicant info
    private String applicantName;
    private String email;
    private String phone;
    
    // 🔥 ADD THIS
    private Long jobSeekerId;

    // 🔹 Application info
    private ApplicationSource applicationSource;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    
    // 🔹 External application link (only if source = EXTERNAL)
    private String externalApplicationUrl;

    // 🔹 Nested minimal objects
    private JobSummaryDTO job;
    private CompanySummaryDTO company;
    // 🔥 ADD THIS
    private JobSeekerSummaryDTO jobSeeker;
}
