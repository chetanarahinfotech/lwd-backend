package com.lwd.jobportal.jobapplicationdto;

import java.time.LocalDateTime;

import com.lwd.jobportal.companydto.CompanySummaryDTO;
import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.jobdto.JobSummaryDTO;

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

    // 🔹 Application info
    private ApplicationSource applicationSource;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    // 🔹 Nested minimal objects
    private JobSummaryDTO job;
    private CompanySummaryDTO company;
}
