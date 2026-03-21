package com.lwd.jobportal.pricing.dto;

import com.lwd.jobportal.pricing.enums.PlanName;
import com.lwd.jobportal.pricing.enums.PlanType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponse {

    private Long id;
    private PlanName name;
    private PlanType type;

    private Double price;
    private Integer durationDays;

    private Boolean active;
}
