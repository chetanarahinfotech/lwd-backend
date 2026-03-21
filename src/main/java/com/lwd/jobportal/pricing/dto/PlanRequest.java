package com.lwd.jobportal.pricing.dto;

import com.lwd.jobportal.pricing.enums.PlanName;
import com.lwd.jobportal.pricing.enums.PlanType;

import lombok.Data;

@Data
public class PlanRequest {

    private PlanName name;
    private PlanType type;

    private Double price;
    private Integer durationDays;

    private Boolean active; // enable/disable
}
