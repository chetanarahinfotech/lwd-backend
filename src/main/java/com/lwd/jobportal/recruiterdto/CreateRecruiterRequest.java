package com.lwd.jobportal.recruiterdto;

import lombok.Data;

// Request when recruiter creates profile
@Data
public class CreateRecruiterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
}

// Response when listing recruiters
