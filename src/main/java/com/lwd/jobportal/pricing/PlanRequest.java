package com.lwd.jobportal.pricing;

import lombok.Data;

@Data
public class PlanRequest {

    private PlanName name;
    private PlanType type;

    private Double price;
    private Integer durationDays;

    private Boolean active; // enable/disable
}
