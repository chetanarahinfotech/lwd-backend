package com.lwd.jobportal.controller;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.jobapplicationdto.ApplicationSearchRequest;
import com.lwd.jobportal.dto.jobapplicationdto.JobApplicationRequest;
import com.lwd.jobportal.dto.jobapplicationdto.PagedApplicationsResponse;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "Job application management APIs")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Apply for job",
            description = "Apply for a job as a job seeker. Returns portal success response or external redirect URL."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid application request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only job seekers can apply")
    })
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PostMapping("/apply")
    public ResponseEntity<?> apply(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job application request",
                    required = true
            )
            @RequestBody @Valid JobApplicationRequest request) {

        Long jobSeekerId = SecurityUtils.getUserId();

        String result = jobApplicationService.applyForJob(request, jobSeekerId);

        if (result.startsWith("http")) {
            return ResponseEntity.ok().body(
                    Map.of(
                            "type", "EXTERNAL",
                            "url", result
                    )
            );
        }

        return ResponseEntity.ok().body(
                Map.of(
                        "type", "PORTAL",
                        "message", result
                )
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get applications by logged-in user role",
            description = "Fetch paginated applications for the logged-in user based on their role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @GetMapping("/my-applications")
    public ResponseEntity<PagedApplicationsResponse> getApplicationsByRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        return ResponseEntity.ok(
                jobApplicationService.getApplicationsByRole(
                        userId,
                        role,
                        page,
                        size
                )
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Search applications",
            description = "Search job applications for the logged-in user based on their role and provided filters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications searched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @PostMapping("/search")
    public ResponseEntity<PagedApplicationsResponse> searchApplications(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Application search request",
                    required = true
            )
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

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get applications by job ID",
            description = "Fetch paginated applications for a specific job."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @Parameters({
            @Parameter(name = "jobId", description = "Job ID"),
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
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

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change application status",
            description = "Change job application status. Accessible to ADMIN, RECRUITER_ADMIN, and RECRUITER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> changeApplicationStatus(
            @Parameter(description = "Application ID")
            @PathVariable Long id,

            @Parameter(description = "New application status")
            @RequestParam ApplicationStatus status
    ) {

        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        jobApplicationService.changeApplicationStatus(id, status, userId, role);

        return ResponseEntity.ok().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my applications",
            description = "Fetch paginated applications for the logged-in job seeker."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only job seekers can access this endpoint")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/my")
    public ResponseEntity<PagedApplicationsResponse> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long jobSeekerId = SecurityUtils.getUserId();
        PagedApplicationsResponse response =
                jobApplicationService.getMyApplications(jobSeekerId, page, size);
        return ResponseEntity.ok(response);
    }
}