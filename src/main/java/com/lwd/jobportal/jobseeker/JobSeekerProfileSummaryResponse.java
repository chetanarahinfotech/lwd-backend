package com.lwd.jobportal.jobseeker;

import lombok.Data;

@Data
public class JobSeekerProfileSummaryResponse {

    private String headline;
    private String about;

    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    private Integer profileCompletion;
}
