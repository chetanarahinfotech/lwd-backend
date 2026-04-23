package com.lwd.jobportal.companyadmin;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.job.RecentJobDTO;
import com.lwd.jobportal.jobapplication.HiringFunnelDTO;
import com.lwd.jobportal.recruiter.RecruiterPerformanceDTO;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY_ADMIN')")
public class CompanyAdminDashboardController {

    private final CompanyAdminDashboardQueryService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<CompanyAdminSummaryDTO> getSummary() {
        Long companyId = getCurrentUsersCompanyId();
        return ResponseEntity.ok(dashboardService.getSummary(companyId));
    }

    @GetMapping("/recruiter-performance")
    public ResponseEntity<List<RecruiterPerformanceDTO>> getRecruiterPerformance() {
        Long companyId = getCurrentUsersCompanyId();
        return ResponseEntity.ok(dashboardService.getRecruiterPerformance(companyId));
    }

    @GetMapping("/recent-jobs")
    public ResponseEntity<List<RecentJobDTO>> getRecentJobs(
            @RequestParam(defaultValue = "5") int size
    ) {
        Long companyId = getCurrentUsersCompanyId();
        return ResponseEntity.ok(dashboardService.getRecentJobs(companyId, size));
    }

    @GetMapping("/hiring-funnel")
    public ResponseEntity<HiringFunnelDTO> getHiringFunnel() {
        Long companyId = getCurrentUsersCompanyId();
        return ResponseEntity.ok(dashboardService.getHiringFunnel(companyId));
    }

    private Long getCurrentUsersCompanyId() {
        Long userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getCompany() == null) {
            throw new RuntimeException("Company not found for current user");
        }

        return user.getCompany().getId();
    }
}