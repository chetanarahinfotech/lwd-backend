package com.lwd.jobportal.companyadmin;

import lombok.Data;
import java.util.List;

import com.lwd.jobportal.job.RecentJobDTO;
import com.lwd.jobportal.jobapplication.HiringFunnelDTO;
import com.lwd.jobportal.recruiter.RecruiterPerformanceDTO;

@Data
public class CompanyAdminDashboardDTO {
    // Summary cards (company level)
    private long totalRecruitersInCompany;
    private long totalJobsPosted;
    private long activeJobs;
    private long closedJobs;
    private long totalApplications;

    // Performance
    private List<RecruiterPerformanceDTO> recruiterPerformance;

    // Recent jobs
    private List<RecentJobDTO> recentJobs;

    // Hiring funnel (company wide)
    private HiringFunnelDTO hiringFunnel;
}