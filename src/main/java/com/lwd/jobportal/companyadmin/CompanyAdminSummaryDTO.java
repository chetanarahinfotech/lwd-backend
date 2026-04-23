package com.lwd.jobportal.companyadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminSummaryDTO {
    private long totalRecruitersInCompany;
    private long totalJobsPosted;
    private long activeJobs;
    private long closedJobs;
    private long totalApplications;
}