package com.lwd.jobportal.pricing.dto;

import com.lwd.jobportal.pricing.enums.LimitType;
import com.lwd.jobportal.pricing.enums.PlanType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeatureResponse {

    private Long id;             // 🔹 Required for update/delete
    private String featureCode;  // 🔹 dynamic string code (like APPLY_JOB)
    private PlanType planType;
    private Boolean enabled;     // 🔹 feature enabled in the plan
    private Integer limitValue;  // 🔹 numeric limit if applicable
    private LimitType limitType; // 🔹 TOTAL / DAILY / MONTHLY etc.
    private String description;  // 🔹 optional human-readable description
}
