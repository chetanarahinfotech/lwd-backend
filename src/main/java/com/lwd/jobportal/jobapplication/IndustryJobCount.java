package com.lwd.jobportal.jobapplication;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndustryJobCount {
    private String industry;
    private long count;
}