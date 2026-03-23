package com.lwd.jobportal.controller;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.jobapplicationdto.*;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.Role;
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
    public ResponseEntity<?> apply(@RequestBody @Valid JobApplicationRequest request) {

        Long jobSeekerId = SecurityUtils.getUserId();

        String result = jobApplicationService.applyForJob(request, jobSeekerId);

        // If result is URL → external redirect
        if (result.startsWith("http")) {
            return ResponseEntity.ok().body(
                    Map.of(
                            "type", "EXTERNAL",
                            "url", result
                    )
            );
        }

        // Portal application success
        return ResponseEntity.ok().body(
                Map.of(
                        "type", "PORTAL",
                        "message", result
                )
        );
    }


    
    
    @GetMapping("/my-applications")
    public ResponseEntity<PagedApplicationsResponse> getApplicationsByRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();
        System.out.println("My application role by");
        return ResponseEntity.ok(
                jobApplicationService.getApplicationsByRole(
                        userId,
                        role,
                        page,
                        size
                )
        );
    }
    
    
    @PostMapping("/search")
    public ResponseEntity<PagedApplicationsResponse> searchApplications(
            @RequestBody ApplicationSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        return ResponseEntity.ok(
                jobApplicationService.searchApplications(userId, role, request, page, size)
        );
    }
    


    // ================= ADMIN ENDPOINTS =================
    @GetMapping("/job/{jobId}")
    public ResponseEntity<PagedApplicationsResponse> getApplicationsByJobId(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedApplicationsResponse response =
                jobApplicationService.getApplicationsByJobId(jobId, page, size);
        return ResponseEntity.ok(response);
    }

    
    // ================= CHANGE STATUS =================
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
    
    
    // ================= JOB SEEKER ENDPOINTS =================
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/my")
    public ResponseEntity<PagedApplicationsResponse> getMyApplications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long jobSeekerId = SecurityUtils.getUserId();
        PagedApplicationsResponse response =
                jobApplicationService.getMyApplications(jobSeekerId, page, size);
        return ResponseEntity.ok(response);
    }

//
//    // ================= COMPANY ADMIN / RECRUITER ENDPOINTS =================
//    @PreAuthorize("hasAnyRole('RECRUITER_ADMIN','RECRUITER')")
//    @GetMapping("/company/job/{jobId}")
//    public ResponseEntity<PagedApplicationsResponse> getApplicationsByJobCompany(
//            @PathVariable Long jobId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Authentication authentication
//    ) {
//        
//    	Long userId = SecurityUtils.getUserId();
//        PagedApplicationsResponse response =
//                jobApplicationService.getApplicationsByJobCompany(jobId, userId, page, size);
//
//        return ResponseEntity.ok(response);
//    }


//    @PreAuthorize("hasAnyRole('RECRUITER_ADMIN','RECRUITER')")
//    @GetMapping("/company")
//    public ResponseEntity<PagedApplicationsResponse> getMyCompanyApplications(
//            Authentication authentication,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        Long userId = SecurityUtils.getUserId();
//        PagedApplicationsResponse response =
//                jobApplicationService.getMyCompanyApplications(userId, page, size);
//        return ResponseEntity.ok(response);
//    }
}
