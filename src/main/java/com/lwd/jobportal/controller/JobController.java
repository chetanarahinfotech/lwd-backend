package com.lwd.jobportal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.jobdto.CreateJobRequest;
import com.lwd.jobportal.jobdto.JobResponse;
import com.lwd.jobportal.jobdto.PagedJobResponse;
import com.lwd.jobportal.service.JobService;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // ==================================================
    // CREATE JOB (RECRUITER)
    // ==================================================
    @PostMapping("/create")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobService.createJobAsRecruiter(request, userId));
    }

    // ==================================================
    // CREATE JOB (ADMIN)
    // ==================================================
    @PostMapping("/admin/company/{companyId}")
    public ResponseEntity<JobResponse> createJobByAdmin(
            @Valid @RequestBody CreateJobRequest request,
            @PathVariable Long companyId,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobService.createJobAsAdmin(request, adminId, companyId));
    }

    // ==================================================
    // UPDATE JOB
    // ==================================================
    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody CreateJobRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                jobService.updateJob(jobId, request, userId)
        );
    }

    // ==================================================
    // DELETE JOB
    // ==================================================
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long jobId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        jobService.deleteJob(jobId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================================================
    // CHANGE JOB STATUS
    // ==================================================
    @PatchMapping("/{jobId}/status")
    public ResponseEntity<JobResponse> changeJobStatus(
            @PathVariable Long jobId,
            @RequestParam JobStatus status,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                jobService.changeJobStatus(jobId, status, userId)
        );
    }

    // ==================================================
    // GET JOB BY ID (PUBLIC)
    // ==================================================
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok(
                jobService.getJobById(jobId)
        );
    }

    // ==================================================
    // GET JOBS BY COMPANY (PUBLIC)
    // ==================================================
    @GetMapping("/company/{companyId}")
    public ResponseEntity<PagedJobResponse> getJobsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(
                jobService.getJobsByCompany(companyId, page)
        );
    }

    // ==================================================
    // GET ALL JOBS (PUBLIC)
    // ==================================================
    @GetMapping
    public ResponseEntity<PagedJobResponse> getAllJobs(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(
                jobService.getAllJobs(page)
        );
    }

    // ==================================================
    // GET LATEST JOBS (PUBLIC)
    // ==================================================
    @GetMapping("/latest")
    public ResponseEntity<List<JobResponse>> getLatestJobs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime lastSeen,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                jobService.getLatestJobs(lastSeen, page, size)
        );
    }
    
    // ==================================================
    // GET JOBS BY INDUSTRY (PUBLIC)
    // ==================================================
    @GetMapping("/industry")
    public ResponseEntity<PagedJobResponse> getJobsByIndustry(
            @RequestParam String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jobService.getJobsByIndustry(industry, page, size));
    }


    // ==================================================
    // SEARCH JOBS (PUBLIC)
    // ==================================================
    @GetMapping("/search")
    public ResponseEntity<PagedJobResponse> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) Integer maxExp,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                jobService.searchJobs(
                        title,
                        location,
                        companyName,
                        minExp,
                        maxExp,
                        jobType,
                        page,
                        size
                )
        );
    }
}
