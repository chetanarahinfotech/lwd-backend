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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
@Tag(name = "Recruiter", description = "Recruiter profile, jobs, applications, and company approval APIs")
public class RecruiterController {

    private final RecruiterService recruiterService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create or update recruiter profile",
            description = "Create or update the profile of the logged-in recruiter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter profile saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/profile")
    public ResponseEntity<RecruiterResponseDTO> createOrUpdateProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Recruiter profile request",
                    required = true
            )
            @RequestBody RecruiterRequestDTO dto) {

        RecruiterResponseDTO response =
                recruiterService.createOrUpdateProfile(dto);

        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my recruiter profile",
            description = "Fetch the profile of the logged-in recruiter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter profile fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<RecruiterResponseDTO> getMyProfile() {

        RecruiterResponseDTO response =
                recruiterService.getMyProfile();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get recruiter profile by user ID",
            description = "Fetch recruiter profile by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter profile fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Recruiter not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<RecruiterResponseDTO> getRecruiterByUserId(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {

        RecruiterResponseDTO response =
                recruiterService.getRecruiterByUserId(userId);

        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my posted jobs",
            description = "Fetch all jobs posted by the logged-in recruiter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access this endpoint")
    })
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/jobs")
    public ResponseEntity<List<JobSummaryDTO>> getMyJobs() {

        List<JobSummaryDTO> jobs = recruiterService.getMyPostedJobs();
        return ResponseEntity.ok(jobs);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Request company approval",
            description = "Send company approval request for the logged-in recruiter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company approval request sent successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping("/request-company/{companyId}")
    public ResponseEntity<String> requestCompanyApproval(
            @Parameter(description = "Company ID")
            @PathVariable Long companyId) {

        recruiterService.requestCompanyApproval(companyId);
        return ResponseEntity.ok("Company approval request sent successfully");
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get applications for a job",
            description = "Fetch paginated job applications for a specific job posted by the logged-in recruiter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only recruiters can access this endpoint"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @Parameters({
            @Parameter(name = "jobId", description = "Job ID"),
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
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

    @Operation(
            summary = "Get recruiter summary by ID",
            description = "Fetch recruiter profile summary by recruiter user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter summary fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Recruiter not found")
    })
    @GetMapping("/{id}/summary")
    public ResponseEntity<RecruiterProfileSummaryDTO> getRecruiterSummary(
            @Parameter(description = "Recruiter user ID")
            @PathVariable Long id) {
        RecruiterProfileSummaryDTO dto = recruiterService.getRecruiterSummary(id);
        return ResponseEntity.ok(dto);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my recruiter summary",
            description = "Fetch profile summary of the logged-in recruiter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter summary fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/summary")
    public ResponseEntity<RecruiterProfileSummaryDTO> getMyRecruiterSummary() {
        Long id = SecurityUtils.getUserId();
        RecruiterProfileSummaryDTO dto = recruiterService.getRecruiterSummary(id);
        return ResponseEntity.ok(dto);
    }
}