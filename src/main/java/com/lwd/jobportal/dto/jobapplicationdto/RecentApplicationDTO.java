package com.lwd.jobportal.dto.jobapplicationdto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentApplicationDTO {
	
	private Long applicationId;
    private String candidateName;     // from JobApplication.fullName or jobSeeker.user.name
    private String jobTitle;
    private String appliedDate;       // appliedAt as string
    private String status;             // APPLICATION_STATUS (e.g., APPLIED, SHORTLISTED)
    private String applicationSource;  // PORTAL or EXTERNAL
}