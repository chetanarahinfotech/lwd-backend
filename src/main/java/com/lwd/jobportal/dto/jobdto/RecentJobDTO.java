package com.lwd.jobportal.dto.jobdto;

import lombok.Data;

@Data
public class RecentJobDTO {
    private String title;
    private String companyName;      // from Company entity
    private String location;
    private String industry;
    private String status;            // OPEN, CLOSED, etc.
    private String posted;            // createdAt as string
}