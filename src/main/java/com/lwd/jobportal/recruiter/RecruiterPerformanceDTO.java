package com.lwd.jobportal.recruiter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterPerformanceDTO {
    private Long recruiterId;
    private String recruiterName;
    private Long jobsPosted;
    private Long applicationsReceived;
    private Long activeJobs;
}