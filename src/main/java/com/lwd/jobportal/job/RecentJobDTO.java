package com.lwd.jobportal.job;

import com.lwd.jobportal.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentJobDTO {
    private String title;
    private String companyName;
    private String location;
    private String industry;
    private LocalDateTime createdAt;
    private JobStatus jobStatus;
}