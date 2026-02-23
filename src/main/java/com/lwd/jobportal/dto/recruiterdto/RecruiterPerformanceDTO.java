package com.lwd.jobportal.dto.recruiterdto;

import lombok.Data;

@Data
public class RecruiterPerformanceDTO {
    private String recruiterName;
    private long jobsPosted;
    private long applicationsReceived;
    private long activeJobs;           // jobs with status = OPEN
}