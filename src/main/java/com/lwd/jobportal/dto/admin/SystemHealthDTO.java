package com.lwd.jobportal.dto.admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemHealthDTO {
    private long activeRecruiters;
    private long jobsExpiringSoon;
    private long jobsWithoutApplications;
    private long pendingApprovals;
}