package com.lwd.jobportal.dto.jobdto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobAnalyticsResponse {

    private JobResponse job;

    private Long totalApplications;

    private Map<String, Long> statusCounts;
}
