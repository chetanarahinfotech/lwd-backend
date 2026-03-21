package com.lwd.jobportal.pricing.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionResponse {

    private String planName;
    private String planType;
    private Double price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
}
