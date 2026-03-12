package com.lwd.jobportal.dto.jobapplicationdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobApplicationRequest {

    @NotNull
    private Long jobId;

    private String fullName;

    @Email
    private String email;

    private String phone;

    private String skills;
    private String coverLetter;
    private String resumeUrl;
    private String externalApplicationUrl;
}
