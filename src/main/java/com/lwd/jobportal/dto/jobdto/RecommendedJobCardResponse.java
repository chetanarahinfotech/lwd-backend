package com.lwd.jobportal.dto.jobdto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendedJobCardResponse {

    private Long id;
    private String title;

    private String companyName;
    private String companyLogo;

    private String location;

    private Integer minExperience;
    private Integer maxExperience;

    private Double minSalary;
    private Double maxSalary;

    private String jobType;
    private String workplaceType;
    private Integer maxNoticePeriod;

    private String shortDescription;

    private Long totalApplications;
    private LocalDateTime createdAt;

    private Integer matchScore;
    private String recommendationSource;
}