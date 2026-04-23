package com.lwd.jobportal.job;

import java.util.List;

import com.lwd.jobportal.company.CompanySummaryDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyJobsResponse {
	private CompanySummaryDTO company;
    private List<JobResponse> jobs;
}
