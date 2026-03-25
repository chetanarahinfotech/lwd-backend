package com.lwd.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.admin.CompanyAdminDTO;
import com.lwd.jobportal.dto.admin.JobAdminDTO;
import com.lwd.jobportal.dto.admin.UserAdminDTO;
import com.lwd.jobportal.dto.admin.UserSearchRequest;
import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterResponse;
import com.lwd.jobportal.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    // ================= USERS =================
    @Operation(
            summary = "Get all users",
            description = "Fetch paginated list of all users. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserAdminDTO>> getAllUsers(
            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 20)")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @Operation(
            summary = "Search users",
            description = "Search users with filters and return paginated results. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users searched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/users/search")
    public ResponseEntity<PagedResponse<UserAdminDTO>> searchUsers(
            @RequestBody(description = "User search filters", required = true)
            @org.springframework.web.bind.annotation.RequestBody UserSearchRequest request,

            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 20)")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.searchUsers(request, page, size));
    }

    @Operation(
            summary = "Block user",
            description = "Block a user by user ID. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User blocked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/users/{id}/block")
    public ResponseEntity<String> blockUser(
            @Parameter(description = "User ID")
            @PathVariable Long id
    ) {
        adminService.blockUser(id);
        return ResponseEntity.ok("User blocked");
    }

    @Operation(
            summary = "Unblock user",
            description = "Unblock a user by user ID. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User unblocked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<String> unblockUser(
            @Parameter(description = "User ID")
            @PathVariable Long id
    ) {
        adminService.unblockUser(id);
        return ResponseEntity.ok("User unblocked");
    }

    // ================= GET RECRUITERS BY COMPANY ID =================
    @Operation(
            summary = "Get recruiters by company ID",
            description = "Fetch paginated list of recruiters for a specific company. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiters fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/company/{companyId}/recruiters")
    public ResponseEntity<PagedResponse<RecruiterResponse>> getRecruitersByCompanyId(
            @Parameter(description = "Company ID")
            @PathVariable Long companyId,

            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 20)")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                adminService.getRecruitersByCompanyId(companyId, page, size)
        );
    }

    // ================= COMPANIES =================
    @Operation(
            summary = "Get all companies",
            description = "Fetch paginated list of all companies. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Companies fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/companies")
    public ResponseEntity<PagedResponse<CompanyAdminDTO>> getAllCompanies(
            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 20)")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.getAllCompanies(page, size));
    }

    @Operation(
            summary = "Block company",
            description = "Block a company by company ID. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company blocked successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/companies/{id}/block")
    public ResponseEntity<String> blockCompany(
            @Parameter(description = "Company ID")
            @PathVariable Long id
    ) {
        adminService.blockCompany(id);
        return ResponseEntity.ok("Company blocked");
    }

    @Operation(
            summary = "Unblock company",
            description = "Unblock a company by company ID. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company unblocked successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/companies/{id}/unblock")
    public ResponseEntity<String> unblockCompany(
            @Parameter(description = "Company ID")
            @PathVariable Long id
    ) {
        adminService.unblockCompany(id);
        return ResponseEntity.ok("Company unblocked");
    }

    // ================= JOBS =================
    @Operation(
            summary = "Get all jobs",
            description = "Fetch paginated list of all jobs. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/jobs")
    public ResponseEntity<PagedResponse<JobAdminDTO>> getAllJobs(
            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 20)")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.getAllJobs(page, size));
    }

    @Operation(
            summary = "Close job",
            description = "Close a job by job ID. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job closed successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/jobs/{id}/close")
    public ResponseEntity<String> closeJob(
            @Parameter(description = "Job ID")
            @PathVariable Long id
    ) {
        adminService.closeJob(id);
        return ResponseEntity.ok("Job closed");
    }
}