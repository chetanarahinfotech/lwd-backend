package com.lwd.jobportal.jobdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.lwd.jobportal.enums.JobType;

@Data
public class CreateJobRequest {
    @NotBlank
    private String title;

    private String description;
    private String location;
    private Double salary;
    private String industry;

    private Integer minExperience;   // new
    private Integer maxExperience;   // new
    private JobType jobType;         // new
}
