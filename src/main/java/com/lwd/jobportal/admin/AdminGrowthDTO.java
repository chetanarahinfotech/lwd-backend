package com.lwd.jobportal.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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