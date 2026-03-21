package com.lwd.jobportal.pricing.dto;

import com.lwd.jobportal.pricing.enums.LimitType;

import lombok.Data;

@Data
public class PlanFeatureRequest {

    private String featureCode;
    private Boolean enabled;
    private Integer limitValue;
    
    private LimitType limitType = LimitType.TOTAL; // default TOTAL
    
    private String description; 
}
