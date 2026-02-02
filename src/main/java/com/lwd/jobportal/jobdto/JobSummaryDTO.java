package com.lwd.jobportal.jobdto;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;

import lombok.*;

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
