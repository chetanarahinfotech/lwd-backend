package com.lwd.jobportal.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.companyadmin.RecruiterResponse;
import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.enums.CompanyStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
            @org.springframework.web.bind.annotation.RequestBody UserSearchRequestDTO request,

            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 20)")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.searchUsers(request, page, size));
    }

    @Operation(
            summary = "Update user status",
            description = "Update a user's status. Admin only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status change"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody @Valid UpdateUserStatusRequest request
    ) {
        System.out.println("request object = " + request);
        System.out.println("request status = " + request.getStatus());

        adminService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok("User status updated successfully");
    }
    
    
    @PatchMapping("/users/{id}/reject")
    public ResponseEntity<String> rejectUser(@PathVariable Long id) {
        adminService.rejectUser(id);
        return ResponseEntity.ok("User rejected successfully");
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
    
    @PatchMapping("/companies/{id}/status")
    public ResponseEntity<String> updateCompanyStatus(
            @PathVariable Long id,
            @RequestParam CompanyStatus status
    ) {
        adminService.updateCompanyStatus(id, status);
        return ResponseEntity.ok("Company status updated to " + status);
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