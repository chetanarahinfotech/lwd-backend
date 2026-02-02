package com.lwd.jobportal.jobapplicationdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobApplicationRequest {

    @NotNull
    private Long jobId;

    @NotBlank
    private String fullName;

    @Email
    private String email;

    @NotBlank
    private String phone;

    private String skills;
    private String coverLetter;
    private String resumeUrl;
}
