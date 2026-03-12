package com.lwd.jobportal.dto.recruiterdto;

import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.JobStatsDTO;
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
