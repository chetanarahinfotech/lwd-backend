package com.lwd.jobportal.dto.jobapplicationdto;

import java.time.LocalDate;
import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.ApplicationStatus;
import lombok.Data;

@Data
public class ApplicationSearchRequest {

    // search keyword: candidate name / job title / company name
    private String keyword;

    // filters
    private ApplicationSource applicationSource;
    private String skills;
    private ApplicationStatus status;

    // date filters
    private String dateFilter; 
    // values: TODAY, LAST_WEEK, LAST_MONTH, SPECIFIC_DATE, CUSTOM_RANGE

    private LocalDate specificDate;
    private LocalDate startDate;
    private LocalDate endDate;
}