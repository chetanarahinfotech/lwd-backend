package com.lwd.jobportal.dto.recruiterdto;


import lombok.Data;
import java.util.List;

import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.JobStatsDTO;

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