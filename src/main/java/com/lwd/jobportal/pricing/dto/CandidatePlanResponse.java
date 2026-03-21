package com.lwd.jobportal.pricing.dto;

import java.util.List;

import com.lwd.jobportal.pricing.enums.PlanName;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CandidatePlanResponse {

    private Long planId;
    private PlanName planName;
    private Double price;
    private Integer durationDays;

    private List<FeatureResponse> features;
}
