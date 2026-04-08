package com.lwd.jobportal.dto.jobdto;

import com.lwd.jobportal.enums.JobType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRecommendationCriteria {

    private String keyword;
    private String location;
    private String companyName;
    private String industry;
    private Integer minExp;
    private Integer maxExp;
    private JobType jobType;

    private String noticePreference;
    private Integer maxNoticePeriod;
    private Boolean lwdPreferred;
}