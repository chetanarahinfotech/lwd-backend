package com.lwd.jobportal.jobdto;

import java.time.LocalDateTime;

import com.lwd.jobportal.companydto.CompanySummaryDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Double salary;
    private String status;
    private String industry;
    private String createdBy;
    private Integer minExperience;
    private Integer maxExperience;
    private String jobType;
    private LocalDateTime createdAt;
    private CompanySummaryDTO company;
    
}
