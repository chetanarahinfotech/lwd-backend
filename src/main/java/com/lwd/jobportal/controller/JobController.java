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
import com.lwd.jobportal.dto.jobdto.JobFullResponse;
import com.lwd.jobportal.dto.jobdto.JobResponse;
import com.lwd.jobportal.dto.jobdto.JobSearchRequest;
import com.lwd.jobportal.dto.jobdto.PagedJobResponse;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.pricing.annotation.RequiresFeature;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "APIs for managing jobs in the LWD platform")
public class JobController {

    private final JobService jobService;

    // ==================================================
    // CREATE JOB (ADMIN)
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create job as admin",
            description = "Create a job for a specific company as an admin user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid job request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only admin can access this endpoint")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/company/{companyId}")
    public ResponseEntity<JobResponse> createJobAsAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job creation request",
                    required = true
            )
            @Valid @RequestBody CreateJobRequest request,

            @Parameter(description = "Company ID")
            @PathVariable Long companyId
    ) {
        Long userId = SecurityUtils.getUserId();
        JobResponse response = jobService.createJobAsAdmin(request, userId, companyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================================================
    // CREATE JOB (RECRUITER / RECRUITER_ADMIN)
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create job as recruiter",
            description = "Create a new job as recruiter or recruiter admin. Recruiter role also requires POST_JOB feature access."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid job request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not allowed to create job")
    })
    @PreAuthorize("hasAnyRole('RECRUITER','RECRUITER_ADMIN')")
    @PostMapping("/create")
    @RequiresFeature(
            value = "POST_JOB",
            roles = { Role.RECRUITER }
    )
    public ResponseEntity<JobResponse> createJobAsRecruiter(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job creation request",
                    required = true
            )
            @Valid @RequestBody CreateJobRequest request
    ) {
        JobResponse response = jobService.createJobAsRecruiter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================================================
    // UPDATE JOB
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update job",
            description = "Update an existing job by job ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid job request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @Parameter(description = "Job ID")
            @PathVariable Long jobId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated job request",
                    required = true
            )
            @Valid @RequestBody CreateJobRequest request
    ) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(jobService.updateJob(jobId, request, userId));
    }

    // ==================================================
    // DELETE JOB
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete job",
            description = "Soft delete a job by job ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "Job ID")
            @PathVariable Long jobId
    ) {
        Long userId = SecurityUtils.getUserId();
        jobService.deleteJob(jobId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================================================
    // CHANGE JOB STATUS
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change job status",
            description = "Change status of a job by job ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PatchMapping("/{jobId}/status")
    public ResponseEntity<JobResponse> changeJobStatus(
            @Parameter(description = "Job ID")
            @PathVariable Long jobId,

            @Parameter(description = "New job status")
            @RequestParam JobStatus status
    ) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(jobService.changeJobStatus(jobId, status, userId));
    }

    // ==================================================
    // GET MY JOBS
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my jobs",
            description = "Fetch paginated jobs created by the logged-in user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Parameter(name = "page", description = "Page number (default = 0)")
    @GetMapping("/my-jobs")
    public ResponseEntity<PagedJobResponse> getMyJobs(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(jobService.getMyJobs(page));
    }

    // ==================================================
    // GET JOBS BY ROLE WITH KEYWORD
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get jobs by role",
            description = "Fetch paginated jobs based on logged-in role with optional keyword filter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "keyword", description = "Keyword filter")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @GetMapping("/my-jobs-by-roll")
    public PagedJobResponse getSearchJobsByRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword
    ) {
        return jobService.searchJobsByRole(keyword, page);
    }

    // ==================================================
    // SEARCH JOBS BY ROLE
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Search jobs by role",
            description = "Search jobs using filters based on logged-in user role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @Parameter(name = "page", description = "Page number (default = 0)")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @PostMapping("/my-jobs-by-role/search")
    public PagedJobResponse searchJobsByRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job search request",
                    required = true
            )
            @RequestBody JobSearchRequest request,

            @RequestParam(defaultValue = "0") int page
    ) {
        return jobService.searchJobsByRole(request, page);
    }

    // ==================================================
    // GET JOBS BY RECRUITER
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get jobs by recruiter",
            description = "Fetch paginated jobs created by a specific recruiter. Accessible to ADMIN and RECRUITER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @Parameters({
            @Parameter(name = "recruiterId", description = "Recruiter ID"),
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/{recruiterId}/jobs")
    public ResponseEntity<PagedResponse<JobResponse>> getJobsByRecruiter(
            @PathVariable Long recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId, page, size));
    }

    // ==================================================
    // GET JOB BY ID
    // ==================================================
    @Operation(
            summary = "Get job by ID",
            description = "Fetch a job by its job ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<JobFullResponse> getJobById(
    		@Parameter(description = "Job ID")
    		@PathVariable Long jobId
    		) {
    	System.out.println("fetch job by Id");
    	return ResponseEntity.ok(jobService.getJobFullById(jobId));
    }
    
//    @GetMapping("/{jobId}")
//    public ResponseEntity<JobResponse> getJobById(
//            @Parameter(description = "Job ID")
//            @PathVariable Long jobId
//    ) {
//        return ResponseEntity.ok(jobService.getJobById(jobId));
//    }
    
    

    // ==================================================
    // GET JOB ANALYTICS
    // ==================================================
    @Operation(
            summary = "Get job analytics",
            description = "Fetch analytics for a job by job ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job analytics fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}/analytics")
    public ResponseEntity<JobAnalyticsResponse> getJobAnalytics(
            @Parameter(description = "Job ID")
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok(jobService.getJobAnalytics(jobId));
    }

    // ==================================================
    // GET JOBS BY COMPANY
    // ==================================================
    @Operation(
            summary = "Get jobs by company",
            description = "Fetch paginated jobs for a specific company"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @Parameters({
            @Parameter(name = "companyId", description = "Company ID"),
            @Parameter(name = "page", description = "Page number (default = 12)")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<PagedJobResponse> getJobsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "12") int page
    ) {
        return ResponseEntity.ok(jobService.getJobsByCompany(companyId, page));
    }

    // ==================================================
    // GET ALL JOBS
    // ==================================================
    @Operation(
            summary = "Get all jobs",
            description = "Fetch paginated list of all jobs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 12)")
    })
    @GetMapping
    public ResponseEntity<PagedJobResponse> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(jobService.getAllJobs(page, size));
    }

    // ==================================================
    // GET TOP CATEGORIES
    // ==================================================
    @Operation(
            summary = "Get top categories",
            description = "Fetch top job industry categories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top categories fetched successfully")
    })
    @Parameter(name = "limit", description = "Maximum number of categories to return (default = 12)")
    @GetMapping("/top-categories")
    public ResponseEntity<List<String>> getTopCategories(
            @RequestParam(defaultValue = "12") int limit
    ) {
        return ResponseEntity.ok(jobService.getTopIndustries(limit));
    }

    // ==================================================
    // GET JOBS BY INDUSTRY
    // ==================================================
    @Operation(
            summary = "Get jobs by industry",
            description = "Fetch paginated jobs filtered by industry"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully")
    })
    @Parameters({
            @Parameter(name = "industry", description = "Industry name"),
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 12)")
    })
    @GetMapping("/industry")
    public ResponseEntity<PagedJobResponse> getJobsByIndustry(
            @RequestParam String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(jobService.getJobsByIndustry(industry, page, size));
    }

    // ==================================================
    // SEARCH JOBS
    // ==================================================
    @Operation(
            summary = "Search jobs with filters",
            description = "Search jobs using keyword, location, company, experience, job type, notice preference, and LWD-specific filters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search request")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedJobResponse> searchJobs(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Search location") @RequestParam(required = false) String location,
            @Parameter(description = "Search industry") @RequestParam(required = false) String industry,
            @Parameter(description = "Search company name") @RequestParam(required = false) String companyName,
            @Parameter(description = "Minimum experience") @RequestParam(required = false) Integer minExp,
            @Parameter(description = "Maximum experience") @RequestParam(required = false) Integer maxExp,
            @Parameter(description = "Job type") @RequestParam(required = false) JobType jobType,
            @Parameter(description = "Notice preference") @RequestParam(required = false) NoticeStatus noticePreference,
            @Parameter(description = "Maximum notice period") @RequestParam(required = false) Integer maxNoticePeriod,
            @Parameter(description = "LWD preferred filter") @RequestParam(required = false) Boolean lwdPreferred,
            @Parameter(description = "Page number (default = 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default = 10)") @RequestParam(defaultValue = "10") int size
    ) {
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

    // ==================================================
    // GET SEARCH SUGGESTIONS
    // ==================================================
    @Operation(
            summary = "Get job search suggestions",
            description = "Fetch autocomplete suggestions based on keyword"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggestions fetched successfully")
    })
    @Parameter(name = "keyword", description = "Keyword for suggestions", required = true)
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(jobService.getSearchSuggestions(keyword));
    }

    // ==================================================
    // GET SUGGESTED JOBS
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get suggested jobs",
            description = "Fetch personalized suggested jobs for the logged-in user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggested jobs fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @GetMapping("/suggested")
    public ResponseEntity<PagedJobResponse> getSuggestedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        PagedJobResponse response = jobService.getSuggestedJobs(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // ==================================================
    // GET SIMILAR JOBS
    // ==================================================
    @Operation(
            summary = "Get similar jobs",
            description = "Fetch similar jobs for a given job ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Similar jobs fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}/similar")
    public ResponseEntity<List<JobResponse>> getSimilarJobs(
            @Parameter(description = "Job ID")
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok(jobService.getSimilarJobs(jobId));
    }

    // ==================================================
    // GET TRENDING JOBS
    // ==================================================
    @Operation(
            summary = "Get trending jobs",
            description = "Fetch trending jobs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trending jobs fetched successfully")
    })
    @GetMapping("/trending")
    public ResponseEntity<List<JobResponse>> getTrendingJobs() {
        return ResponseEntity.ok(jobService.getTrendingJobs());
    }

    // ==================================================
    // GET RECOMMENDED JOBS
    // ==================================================
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get recommended jobs",
            description = "Fetch personalized recommended jobs based on logged-in user's profile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommended jobs fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @GetMapping("/recommended")
    public PagedResponse<JobResponse> getRecommendedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return jobService.getRecommendedJobs(page, size);
    }
}