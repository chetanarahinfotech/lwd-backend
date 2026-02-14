package com.lwd.jobportal.dto.jobapplicationdto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagedApplicationsResponse {

    private List<JobApplicationResponse> applications;

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
