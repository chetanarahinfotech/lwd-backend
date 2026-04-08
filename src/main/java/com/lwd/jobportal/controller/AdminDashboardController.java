package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.admin.*;
import com.lwd.jobportal.dto.jobapplicationdto.DailyApplication;
import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.RecentJobDTO;
import com.lwd.jobportal.dto.userdto.RecentUserDTO;
import com.lwd.jobportal.service.AdminDashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardQueryService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/growth")
    public ResponseEntity<AdminGrowthDTO> getGrowth() {
        return ResponseEntity.ok(dashboardService.getGrowth());
    }

    @GetMapping("/recent-users")
    public ResponseEntity<List<RecentUserDTO>> getRecentUsers(
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(dashboardService.getRecentUsers(size));
    }

    @GetMapping("/recent-jobs")
    public ResponseEntity<List<RecentJobDTO>> getRecentJobs(
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(dashboardService.getRecentJobs(size));
    }

    @GetMapping("/recent-applications")
    public ResponseEntity<List<RecentApplicationDTO>> getRecentApplications(
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(dashboardService.getRecentApplications(size));
    }

    @GetMapping("/charts/jobs-per-industry")
    public ResponseEntity<Map<String, Long>> getJobsPerIndustry() {
        return ResponseEntity.ok(dashboardService.getJobsPerIndustry());
    }

    @GetMapping("/charts/applications-trend")
    public ResponseEntity<List<DailyApplication>> getApplicationsTrend() {
        return ResponseEntity.ok(dashboardService.getApplicationsTrend());
    }

    @GetMapping("/charts/users-by-role")
    public ResponseEntity<Map<String, Long>> getUsersByRole() {
        return ResponseEntity.ok(dashboardService.getUsersByRole());
    }

    @GetMapping("/system-health")
    public ResponseEntity<SystemHealthDTO> getSystemHealth() {
        return ResponseEntity.ok(dashboardService.getSystemHealth());
    }
}