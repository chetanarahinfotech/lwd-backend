package com.lwd.jobportal.dto.jobdto;

import lombok.Data;

@Data
public class JobStatsDTO {
    private String jobTitle;
    private long applications;
    private long shortlisted;           // status = SHORTLISTED
    private long rejected;               // status = REJECTED
    private long pending;                 // status = APPLIED (or APPLIED + INTERVIEW)
    private long interview;                // optional
}