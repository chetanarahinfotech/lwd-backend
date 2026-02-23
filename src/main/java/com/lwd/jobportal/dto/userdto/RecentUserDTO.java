package com.lwd.jobportal.dto.userdto;

import lombok.Data;

@Data
public class RecentUserDTO {
    private String name;
    private String email;
    private String role;        // e.g., JOB_SEEKER, RECRUITER
    private String joined;       // LocalDate as string (e.g., "2025-02-20")
    private String status;       // ACTIVE, PENDING, BLOCKED (optional)
}