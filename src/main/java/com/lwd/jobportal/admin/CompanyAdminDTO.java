package com.lwd.jobportal.admin;

import lombok.*;
import java.time.LocalDateTime;

import com.lwd.jobportal.enums.CompanyStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminDTO {

    private Long id;
    private String companyName;

    private CompanyStatus status;
    private Boolean verified;

    private Long createdById;
    private String createdByName;

    private long totalRecruiters;
    private long totalCompanyAdmins;
    private long totalJobs;

    private LocalDateTime createdAt;
}