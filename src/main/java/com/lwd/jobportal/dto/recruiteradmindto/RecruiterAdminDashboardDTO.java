package com.lwd.jobportal.dto.recruiteradmindto;

import lombok.Data;
import java.util.List;

import com.lwd.jobportal.dto.jobapplicationdto.HiringFunnelDTO;
import com.lwd.jobportal.dto.jobdto.RecentJobDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterPerformanceDTO;

@Data
public class RecruiterAdminDashboardDTO {
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