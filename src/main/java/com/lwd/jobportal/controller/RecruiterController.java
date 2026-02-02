package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.jobdto.JobSummaryDTO;
import com.lwd.jobportal.jobapplicationdto.PagedApplicationsResponse;
import com.lwd.jobportal.service.RecruiterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;

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
}
