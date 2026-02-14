package com.lwd.jobportal.dto.jobdto;

import java.util.List;

import com.lwd.jobportal.dto.companydto.CompanySummaryDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyJobsResponse {
	private CompanySummaryDTO company;
    private List<JobResponse> jobs;
}
