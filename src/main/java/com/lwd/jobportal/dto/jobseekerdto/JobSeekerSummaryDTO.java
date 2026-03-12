package com.lwd.jobportal.dto.jobseekerdto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobSeekerSummaryDTO {

    private Long id;

    private String headline;

    private Integer totalExperience;

    private String currentCompany;

    private String currentLocation;

    private Double currentCTC;

    private Double expectedCTC;

    private Boolean immediateJoiner;

    private Integer noticePeriod;

    private String resumeUrl;

}
