package com.lwd.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterResponse;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.RecruiterAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recruiter-admin")
@PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Recruiter Admin", description = "Recruiter admin management APIs")
@SecurityRequirement(name = "bearerAuth")
public class RecruiterAdminController {

    private final RecruiterAdminService recruiterAdminService;

    @Operation(
            summary = "Get company recruiters",
            description = "Fetch paginated list of recruiters belonging to the logged-in recruiter admin's company"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiters fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @GetMapping("/recruiters")
    public ResponseEntity<PagedResponse<RecruiterResponse>> getCompanyRecruiters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long recruiterAdminId = SecurityUtils.getUserId();
        return ResponseEntity.ok(
                recruiterAdminService.getCompanyRecruiters(recruiterAdminId, page, size)
        );
    }

    @Operation(
            summary = "Get pending recruiters",
            description = "Fetch paginated list of pending recruiters. Accessible to ADMIN and RECRUITER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending recruiters fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (default = 0)"),
            @Parameter(name = "size", description = "Page size (default = 10)")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/recruiters/pending")
    public ResponseEntity<PagedResponse<RecruiterResponse>> getPendingRecruiters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Long adminId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                recruiterAdminService.getPendingRecruiters(adminId, page, size)
        );
    }

    @Operation(
            summary = "Approve recruiter",
            description = "Approve recruiter registration by recruiter ID. Accessible to ADMIN and RECRUITER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter approved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Recruiter not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PutMapping("/recruiters/{id}/approve")
    public ResponseEntity<RecruiterResponse> approveRecruiter(
            @Parameter(description = "Recruiter ID")
            @PathVariable Long id
    ) {

        Long adminId = SecurityUtils.getUserId();

        RecruiterResponse response =
                recruiterAdminService.approveRecruiter(id, adminId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Block or unblock recruiter",
            description = "Block or unblock a recruiter by recruiter ID. Accessible to ADMIN and RECRUITER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid block request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Recruiter not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PutMapping("/recruiters/{id}/block")
    public ResponseEntity<RecruiterResponse> blockRecruiter(
            @Parameter(description = "Recruiter ID")
            @PathVariable Long id,

            @Parameter(description = "Block recruiter if true, unblock if false")
            @RequestParam boolean block
    ) {

        RecruiterResponse response = recruiterAdminService.blockRecruiter(id, block);
        return ResponseEntity.ok(response);
    }
}