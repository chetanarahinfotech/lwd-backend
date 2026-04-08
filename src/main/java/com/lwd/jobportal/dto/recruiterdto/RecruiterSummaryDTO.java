package com.lwd.jobportal.dto.recruiterdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterSummaryDTO {
    private long myPostedJobs;
    private long myActiveJobs;
    private long totalApplications;
    private long interviewsScheduled;
    private long shortlistedCandidates;
}