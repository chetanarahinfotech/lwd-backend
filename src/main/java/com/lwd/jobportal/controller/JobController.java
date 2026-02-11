package com.lwd.jobportal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.jobdto.CreateJobRequest;
import com.lwd.jobportal.jobdto.JobResponse;
import com.lwd.jobportal.jobdto.PagedJobResponse;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobService;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    
    
    // ==================================================
    // CREATE JOB (ADMIN)
    // ==================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/company/{companyId}")
    public ResponseEntity<JobResponse> createJobAsAdmin(
            @Valid @RequestBody CreateJobRequest request,
            @PathVariable Long companyId
    ) {
    	Long userId = SecurityUtils.getUserId();
        JobResponse response = jobService.createJobAsAdmin(request, userId, companyId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ==================================================
    // CREATE JOB (RECRUITER / RECRUITER_ADMIN)
    // ==================================================
    @PreAuthorize("hasAnyRole('RECRUITER','RECRUITER_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<JobResponse> createJobAsRecruiter(
            @Valid @RequestBody CreateJobRequest request
    ) {
        JobResponse response = jobService.createJobAsRecruiter(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

   
    // ==================================================
    // UPDATE JOB
    // ==================================================
    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody CreateJobRequest request
    ) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(
                jobService.updateJob(jobId, request, userId)
        );
    }

    // ==================================================
    // DELETE JOB
    // ==================================================
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long jobId
    ) {
        Long userId = SecurityUtils.getUserId();
        jobService.deleteJob(jobId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================================================
    // CHANGE JOB STATUS
    // ==================================================
    @PatchMapping("/{jobId}/status")
    public ResponseEntity<JobResponse> changeJobStatus(
            @PathVariable Long jobId,
            @RequestParam JobStatus status
    ) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(
                jobService.changeJobStatus(jobId, status, userId)
        );
    }
    
    // ==================================================
    // GET JOB
    // ==================================================
    @GetMapping("/my-jobs")
    public ResponseEntity<PagedJobResponse> getMyJobs(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(jobService.getMyJobs(page));
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
    
    @GetMapping("/filter")
    public ResponseEntity<PagedJobResponse> filterJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) Integer maxExp,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                jobService.filterJobs(
                        location,
                        jobType,
                        minExp,
                        maxExp,
                        status,
                        page,
                        size
                )
        );
    }

    
    @GetMapping("/quick-search")
    public ResponseEntity<PagedJobResponse> quickSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                jobService.quickSearch(q, page, size)
        );
    }
    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/suggested")
    public ResponseEntity<List<JobResponse>> getSuggestedJobs() {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(
                jobService.getSuggestedJobs(userId)
        );
    }
    
    @GetMapping("/{jobId}/similar")
    public ResponseEntity<List<JobResponse>> getSimilarJobs(
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok(
                jobService.getSimilarJobs(jobId)
        );
    }

    
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                jobService.getSearchSuggestions(keyword)
        );
    }
    
    @GetMapping("/trending")
    public ResponseEntity<List<JobResponse>> getTrendingJobs() {
        return ResponseEntity.ok(
                jobService.getTrendingJobs()
        );
    }

    
//    @GetMapping("/location/{city}")
//    public ResponseEntity<PagedJobResponse> getJobsByCity(
//            @PathVariable String city,
//            @RequestParam(defaultValue = "0") int page
//    ) {
//        return ResponseEntity.ok(
//                jobService.getJobsByCity(city, page)
//        );
//    }




}
