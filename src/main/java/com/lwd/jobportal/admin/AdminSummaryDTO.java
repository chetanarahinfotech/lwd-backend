package com.lwd.jobportal.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSummaryDTO {
    private long totalUsers;
    private long totalCompanies;
    private long totalJobs;
    private long totalApplications;
    private long totalRecruiters;
    private long activeJobs;
}