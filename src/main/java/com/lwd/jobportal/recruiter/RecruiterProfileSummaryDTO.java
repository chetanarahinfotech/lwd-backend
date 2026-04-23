package com.lwd.jobportal.recruiter;

import com.lwd.jobportal.job.JobStatsDTO;
import com.lwd.jobportal.jobapplication.RecentApplicationDTO;

import lombok.Data;

import java.util.List;

@Data
public class RecruiterProfileSummaryDTO {

    // Basic recruiter info
    private String name;
    private String designation;
    private Integer experience;
    private String location;
    private String phone;
    private String linkedinUrl;
    private String about;

    // Performance metrics
    private long myPostedJobs;
    private long myActiveJobs;
    private long totalApplications;
    private long interviewsScheduled;
    private long shortlistedCandidates;

    // Per-job statistics
    private List<JobStatsDTO> perJobStats;

    // Recent applications
    private List<RecentApplicationDTO> recentApplications;
}
