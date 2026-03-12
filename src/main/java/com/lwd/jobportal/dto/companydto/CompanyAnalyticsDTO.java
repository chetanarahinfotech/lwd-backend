package com.lwd.jobportal.dto.companydto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyAnalyticsDTO {

    private Long totalJobs;
    private Long totalRecruiters;
    private Long totalApplications;
    private Long activeJobs;
    private Long closedJobs;
}
