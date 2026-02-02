package com.lwd.jobportal.jobdto;

import java.util.List;

import com.lwd.jobportal.companydto.CompanySummaryDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyJobsResponse {
	private CompanySummaryDTO company;
    private List<JobResponse> jobs;
}
