package com.lwd.jobportal.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.jobapplicationdto.*;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobApplicationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    // ================= APPLY FOR JOB =================
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PostMapping("/apply")
    public ResponseEntity<String> applyForJob(
            @Valid @RequestBody JobApplicationRequest request,
            Authentication authentication
    ) {
        Long jobSeekerId = (Long) authentication.getPrincipal();
        jobApplicationService.applyForJob(request, jobSeekerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Job application submitted successfully");
    }

    // ================= ADMIN ENDPOINTS =================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/job/{jobId}")
    public ResponseEntity<PagedApplicationsResponse> getApplicationsByJobAdmin(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedApplicationsResponse response =
                jobApplicationService.getApplicationsByJobAdmin(jobId, page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> changeApplicationStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status
    ) {

        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        jobApplicationService.changeApplicationStatus(id, status, userId, role);

        return ResponseEntity.ok().build();
    }


    // ================= COMPANY ADMIN / RECRUITER ENDPOINTS =================
    @PreAuthorize("hasAnyRole('RECRUITER_ADMIN','RECRUITER')")
    @GetMapping("/company/job/{jobId}")
    public ResponseEntity<PagedApplicationsResponse> getApplicationsByJobCompany(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        
    	Long userId = (Long) authentication.getPrincipal();
        PagedApplicationsResponse response =
                jobApplicationService.getApplicationsByJobCompany(jobId, userId, page, size);

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyRole('RECRUITER_ADMIN','RECRUITER')")
    @GetMapping("/company")
    public ResponseEntity<PagedApplicationsResponse> getMyCompanyApplications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = (Long) authentication.getPrincipal();
        PagedApplicationsResponse response =
                jobApplicationService.getMyCompanyApplications(userId, page, size);
        return ResponseEntity.ok(response);
    }
    


    // ================= JOB SEEKER ENDPOINTS =================
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/my")
    public ResponseEntity<PagedApplicationsResponse> getMyApplications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long jobSeekerId = (Long) authentication.getPrincipal();
        PagedApplicationsResponse response =
                jobApplicationService.getMyApplications(jobSeekerId, page, size);
        return ResponseEntity.ok(response);
    }
}
