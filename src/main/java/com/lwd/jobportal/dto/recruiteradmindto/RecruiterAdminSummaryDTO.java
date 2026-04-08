package com.lwd.jobportal.dto.recruiteradmindto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterAdminSummaryDTO {
    private long totalRecruitersInCompany;
    private long totalJobsPosted;
    private long activeJobs;
    private long closedJobs;
    private long totalApplications;
}