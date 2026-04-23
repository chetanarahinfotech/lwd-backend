package com.lwd.jobportal.recruiter;


import lombok.Data;
import java.util.List;

import com.lwd.jobportal.job.JobStatsDTO;
import com.lwd.jobportal.jobapplication.RecentApplicationDTO;

@Data
public class RecruiterDashboardDTO {
    // Summary cards (personal)
    private long myPostedJobs;
    private long myActiveJobs;
    private long totalApplications;
    private long interviewsScheduled;       // status = INTERVIEW
    private long shortlistedCandidates;     // status = SHORTLISTED

    // Per job statistics
    private List<JobStatsDTO> perJobStats;

    // Recent applications (to my jobs)
    private List<RecentApplicationDTO> recentApplications;
}