package com.lwd.jobportal.companyadmin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.admin.UpdateUserStatusRequest;
import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company-admin")
@PreAuthorize("hasAnyRole('ADMIN','COMPANY_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Recruiter Admin", description = "Recruiter admin management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CompanyAdminController {

    private final CompanyAdminService CompanyAdminService;

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
        Long CompanyAdminId = SecurityUtils.getUserId();
        return ResponseEntity.ok(
                CompanyAdminService.getCompanyRecruiters(CompanyAdminId, page, size)
        );
    }

    @Operation(
            summary = "Get pending recruiters",
            description = "Fetch paginated list of pending recruiters. Accessible to ADMIN and COMPANY_ADMIN."
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
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY_ADMIN')")
    @GetMapping("/recruiters/pending")
    public ResponseEntity<PagedResponse<RecruiterResponse>> getPendingRecruiters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Long adminId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                CompanyAdminService.getPendingRecruiters(adminId, page, size)
        );
    }

    @PatchMapping("/recruiters/{id}/status")
    public ResponseEntity<String> updateRecruiterStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserStatusRequest request
    ) {
    	CompanyAdminService.updateRecruiterStatus(id, request.getStatus());
        return ResponseEntity.ok("Recruiter status updated successfully");
    }
    
    @PutMapping("/recruiters/{recruiterId}/reject")
    public ResponseEntity<UserApprovalResponse> rejectRecruiter(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(CompanyAdminService.rejectRecruiter(recruiterId));
    }
}