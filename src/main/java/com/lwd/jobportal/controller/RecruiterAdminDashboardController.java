package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.jobapplicationdto.HiringFunnelDTO;
import com.lwd.jobportal.dto.jobdto.RecentJobDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterPerformanceDTO;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterAdminSummaryDTO;
import com.lwd.jobportal.service.RecruiterAdminDashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter-admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER_ADMIN')")
public class RecruiterAdminDashboardController {

    private final RecruiterAdminDashboardQueryService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<RecruiterAdminSummaryDTO> getSummary() {
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