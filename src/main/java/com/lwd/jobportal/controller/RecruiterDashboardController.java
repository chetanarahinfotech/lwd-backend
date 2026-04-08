package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.JobStatsDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterSummaryDTO;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.RecruiterDashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterDashboardController {

    private final RecruiterDashboardQueryService recruiterDashboardQueryService;

    @GetMapping("/summary")
    public ResponseEntity<RecruiterSummaryDTO> getSummary() {
        Long recruiterId = SecurityUtils.getUserId();
        return ResponseEntity.ok(recruiterDashboardQueryService.getSummary(recruiterId));
    }

    @GetMapping("/per-job-stats")
    public ResponseEntity<List<JobStatsDTO>> getPerJobStats() {
        Long recruiterId = SecurityUtils.getUserId();
        return ResponseEntity.ok(recruiterDashboardQueryService.getPerJobStats(recruiterId));
    }

    @GetMapping("/recent-applications")
    public ResponseEntity<List<RecentApplicationDTO>> getRecentApplications(
            @RequestParam(defaultValue = "5") int size
    ) {
        Long recruiterId = SecurityUtils.getUserId();
        return ResponseEntity.ok(recruiterDashboardQueryService.getRecentApplications(recruiterId, size));
    }
}