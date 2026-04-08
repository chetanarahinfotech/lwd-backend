package com.lwd.jobportal.dto.admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminGrowthDTO {
    private long usersThisMonth;
    private long jobsThisMonth;
    private long applicationsThisWeek;
    private long newCompaniesThisMonth;
}