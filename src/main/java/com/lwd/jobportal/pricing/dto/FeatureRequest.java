package com.lwd.jobportal.pricing.dto;

import com.lwd.jobportal.pricing.enums.PlanType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeatureRequest {
	
	@NotBlank(message = "Feature code is required")
    private String code;        // like APPLY_JOB, POST_JOB
	
	@NotNull(message = "Plan type is required")
    private PlanType planType;  // JOB_SEEKER / RECRUITER
    private String description; // optional human-readable description
}
