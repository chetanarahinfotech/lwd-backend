package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.resume.ResumeViewHistoryResponse;
import com.lwd.jobportal.dto.resume.ResumeViewStatsResponse;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.ResumeViewHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume-views")
@RequiredArgsConstructor
public class ResumeViewHistoryController {

    private final ResumeViewHistoryService resumeViewHistoryService;

    // ===============================
    // ✅ 1. LOG VIEW (MAIN API)
    // ===============================
    @PostMapping("/log")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITER','RECRUITER_ADMIN')")
    public ResponseEntity<String> logResumeView(
            @RequestParam Long resumeId,
            @RequestParam Long ownerUserId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long applicationId,
            @RequestParam String viewSource,
            HttpServletRequest request
    ) {

        Long viewerId = SecurityUtils.getUserId();
        Role viewerRole = SecurityUtils.getRole();

        resumeViewHistoryService.logResumeView(
                resumeId,
                ownerUserId,
                viewerId,
                viewerRole,
                jobId,
                applicationId,
                viewSource,
                request
        );

        return ResponseEntity.ok("View logged successfully");
    }

    // ===============================
    // ✅ 2. GET HISTORY BY RESUME
    // ===============================
    @GetMapping("/resume/{resumeId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','RECRUITER','RECRUITER_ADMIN')")
    public ResponseEntity<Page<ResumeViewHistoryResponse>> getResumeViewHistory(
            @PathVariable Long resumeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                resumeViewHistoryService.getResumeViewHistory(resumeId, pageable)
        );
    }

    // ===============================
    // ✅ 3. GET HISTORY FOR OWNER
    // ===============================
    @GetMapping("/owner/{ownerUserId}")
    @PreAuthorize("hasAnyRole('JOB_SEEKER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Page<ResumeViewHistoryResponse>> getOwnerHistory(
            @PathVariable Long ownerUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                resumeViewHistoryService.getOwnerResumeViewHistory(ownerUserId, pageable)
        );
    }

    // ===============================
    // ✅ 4. GET STATS
    // ===============================
    @GetMapping("/stats/{resumeId}")
    @PreAuthorize("hasAnyRole('JOB_SEEKER','ADMIN','SUPER_ADMIN','RECRUITER','RECRUITER_ADMIN')")
    public ResponseEntity<ResumeViewStatsResponse> getStats(
            @PathVariable Long resumeId
    ) {
        return ResponseEntity.ok(
                resumeViewHistoryService.getResumeViewStats(resumeId)
        );
    }
}