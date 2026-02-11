package com.lwd.jobportal.dto.admin;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyAdminDTO {

    private Long id;
    private String companyName;

    // status
    private Boolean isActive;

    // audit
    private Long createdById;
    private String createdByName;

    // stats
    private Long totalRecruiters;
    private Long totalJobs;
}
