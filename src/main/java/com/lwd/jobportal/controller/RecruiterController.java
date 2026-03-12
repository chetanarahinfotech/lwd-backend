package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.jobapplicationdto.PagedApplicationsResponse;
import com.lwd.jobportal.dto.jobdto.JobSummaryDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterProfileSummaryDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterRequestDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterResponseDTO;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.RecruiterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;

    // =====================================================
    // CREATE / UPDATE PROFILE
    // =====================================================

    @PostMapping("/profile")
    public ResponseEntity<RecruiterResponseDTO> createOrUpdateProfile(
            @RequestBody RecruiterRequestDTO dto) {

        RecruiterResponseDTO response =
                recruiterService.createOrUpdateProfile(dto);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // GET LOGGED-IN RECRUITER PROFILE
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<RecruiterResponseDTO> getMyProfile() {

        RecruiterResponseDTO response =
                recruiterService.getMyProfile();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // GET RECRUITER PROFILE BY USER ID
    // =====================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<RecruiterResponseDTO> getRecruiterByUserId(
            @PathVariable Long userId) {

        RecruiterResponseDTO response =
                recruiterService.getRecruiterByUserId(userId);

        return ResponseEntity.ok(response);
    }

    // ================= GET ALL JOBS POSTED BY LOGGED-IN RECRUITER =================
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/jobs")
    public ResponseEntity<List<JobSummaryDTO>> getMyJobs() {

        List<JobSummaryDTO> jobs = recruiterService.getMyPostedJobs();
        return ResponseEntity.ok(jobs);
    }

    // ================= REQUEST COMPANY APPROVAL =================
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping("/request-company/{companyId}")
    public ResponseEntity<String> requestCompanyApproval(
            @PathVariable Long companyId) {

        recruiterService.requestCompanyApproval(companyId);
        return ResponseEntity.ok("Company approval request sent successfully");
    }

    // ================= GET APPLICATIONS FOR A SPECIFIC JOB =================
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<PagedApplicationsResponse> getApplicationsForJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedApplicationsResponse response =
                recruiterService.getApplicationsForJob(jobId, page, size);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/summary")
    public ResponseEntity<RecruiterProfileSummaryDTO> getRecruiterSummary(@PathVariable Long id) {
        RecruiterProfileSummaryDTO dto = recruiterService.getRecruiterSummary(id);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<RecruiterProfileSummaryDTO> getMyRecruiterSummary() {
    	Long id = SecurityUtils.getUserId();
        RecruiterProfileSummaryDTO dto = recruiterService.getRecruiterSummary(id);
        return ResponseEntity.ok(dto);
    }
}
