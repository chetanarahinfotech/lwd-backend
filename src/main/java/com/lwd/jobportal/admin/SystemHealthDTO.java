package com.lwd.jobportal.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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