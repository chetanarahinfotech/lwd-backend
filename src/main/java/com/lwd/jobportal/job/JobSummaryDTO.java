package com.lwd.jobportal.job;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobSummaryDTO {

    private Long id;
    private String title;
    private String location;

    private JobType jobType;
    private Integer minExperience;
    private Integer maxExperience;

    private JobStatus status;
    private LocalDateTime createdAt;
}
