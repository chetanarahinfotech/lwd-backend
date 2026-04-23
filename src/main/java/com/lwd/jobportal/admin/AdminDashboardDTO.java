package com.lwd.jobportal.admin;

import lombok.Data;
import java.util.List;
import java.util.Map;

import com.lwd.jobportal.job.RecentJobDTO;
import com.lwd.jobportal.jobapplication.DailyApplication;
import com.lwd.jobportal.jobapplication.RecentApplicationDTO;
import com.lwd.jobportal.user.RecentUserDTO;

@Data
public class AdminDashboardDTO {
    // KPI cards
    private long totalUsers;
    private long totalCompanies;
    private long totalJobs;
    private long totalApplications;
    private long totalRecruiters;          // RECRUITER + COMPANY_ADMIN
    private long activeJobs;                // JobStatus.OPEN

    // Growth metrics
    private long usersThisMonth;
    private long jobsThisMonth;
    private long applicationsThisWeek;
    private long newCompaniesThisMonth;

    // Recent activity
    private List<RecentUserDTO> recentUsers;
    private List<RecentJobDTO> recentJobs;
    private List<RecentApplicationDTO> recentApplications;

    // Charts
    private Map<String, Long> jobsPerIndustry;      // industry -> count
    private List<DailyApplication> applicationsTrend; // last 7 days
    private Map<String, Long> usersByRole;           // role name -> count

    // System health
    private long activeRecruiters;                     // same as totalRecruiters (optional)
    private long jobsExpiringSoon;                      // if you have expiry logic
    private long jobsWithoutApplications;                // jobs with zero applications
    private long pendingApprovals;                       // if any

}