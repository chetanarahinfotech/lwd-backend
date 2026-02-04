package com.lwd.jobportal.recruiterdto;

import com.lwd.jobportal.enums.UserStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecruiterResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private String companyName; // Company of the recruiter
}
