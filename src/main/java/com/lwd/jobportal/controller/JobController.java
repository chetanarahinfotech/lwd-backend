package com.lwd.jobportal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.jobdto.CreateJobRequest;
import com.lwd.jobportal.dto.jobdto.JobAnalyticsResponse;
import com.lwd.jobportal.dto.jobdto.JobResponse;
import com.lwd.jobportal.dto.jobdto.PagedJobResponse;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.pricing.annotation.RequiresFeature;
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
    @RequiresFeature(
    	    value = "POST_JOB",
    	    roles = { Role.RECRUITER } // only recruiters can post jobs
    	)
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
    public ResponseEntity<Void> deleteJob( @PathVariable Long jobId ) {
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
    
    
    @GetMapping("/my-jobs-by-roll")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    public PagedJobResponse getSearchJobsByRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword
    ) {
        return jobService.searchJobsByRole(keyword, page);
    }
    
    
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/{recruiterId}/jobs")
    public ResponseEntity<PagedResponse<JobResponse>> getJobsByRecruiter(
            @PathVariable Long recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
        		jobService.getJobsByRecruiter(recruiterId, page, size)
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
    
    @GetMapping("/{jobId}/analytics")
    public ResponseEntity<JobAnalyticsResponse> getJobAnalytics(
            @PathVariable Long jobId) {

        return ResponseEntity.ok(jobService.getJobAnalytics(jobId));
    }


    // ==================================================
    // GET JOBS BY COMPANY (PUBLIC)
    // ==================================================
    @GetMapping("/company/{companyId}")
    public ResponseEntity<PagedJobResponse> getJobsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "12") int page
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
	         @RequestParam(defaultValue = "0") int page,
	         @RequestParam(defaultValue = "12") int size
	 ) {
		 System.out.println("Get all Jobs");
	     return ResponseEntity.ok(
	             jobService.getAllJobs(page, size)
	     );
	 }

    
    // ==================================================
    // GET INDUSTRY CATEGORIES
    // ==================================================
    @GetMapping("/top-categories")
    public ResponseEntity<List<String>> getTopCategories(
            @RequestParam(defaultValue = "12") int limit
    ) {
    	System.out.println("Top Categaries");
        return ResponseEntity.ok(
                jobService.getTopIndustries(limit)
        );
    }

    
    // ==================================================
    // GET JOBS BY INDUSTRY (PUBLIC)
    // ==================================================
    @GetMapping("/industry")
    public ResponseEntity<PagedJobResponse> getJobsByIndustry(
            @RequestParam String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
    	System.out.println("Industry");
        return ResponseEntity.ok(jobService.getJobsByIndustry(industry, page, size));
    }



    @GetMapping("/search")
    public ResponseEntity<PagedJobResponse> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) Integer maxExp,
            @RequestParam(required = false) JobType jobType,

            // ===== LWD FILTERS =====
            @RequestParam(required = false) NoticeStatus noticePreference,
            @RequestParam(required = false) Integer maxNoticePeriod,
            @RequestParam(required = false) Boolean lwdPreferred,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
    	System.out.println("Search jobs");
        return ResponseEntity.ok(
                jobService.searchPublicJobs(
                        keyword,
                        location,
                        industry,
                        companyName,
                        minExp,
                        maxExp,
                        jobType,
                        noticePreference,
                        maxNoticePeriod,
                        lwdPreferred,
                        page,
                        size
                )
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
    
    
    
    @GetMapping("/suggested")
    public ResponseEntity<PagedJobResponse> getSuggestedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        PagedJobResponse response = jobService.getSuggestedJobs(userId, page, size);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/{jobId}/similar")
    public ResponseEntity<List<JobResponse>> getSimilarJobs(
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok(
                jobService.getSimilarJobs(jobId)
        );
    }

    
    @GetMapping("/trending")
    public ResponseEntity<List<JobResponse>> getTrendingJobs() {
        return ResponseEntity.ok(
                jobService.getTrendingJobs()
        );
    }

    
    @GetMapping("/recommended")
    public PagedResponse<JobResponse> getRecommendedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return jobService.getRecommendedJobs(page, size);
    }




}
