package com.lwd.jobportal.dto.authdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role; // JOB_SEEKER / RECRUITER / RECRUITER_ADMIN
}
