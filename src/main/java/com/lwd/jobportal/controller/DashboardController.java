package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.admin.AdminDashboardDTO;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterAdminDashboardDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterDashboardDTO;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.AdminDashboardService;
import com.lwd.jobportal.service.RecruiterAdminDashboardService;
import com.lwd.jobportal.service.RecruiterDashboardService;
import com.lwd.jobportal.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard APIs for admin, recruiter admin, and recruiter")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final AdminDashboardService adminService;
    private final RecruiterAdminDashboardService recruiterAdminService;
    private final RecruiterDashboardService recruiterService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Get admin dashboard",
            description = "Fetch dashboard statistics and summary for admin user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin dashboard fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    @Operation(
            summary = "Get recruiter admin dashboard",
            description = "Fetch dashboard statistics and summary for recruiter admin based on their company"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter admin dashboard fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Company or user not found")
    })
    @GetMapping("/recruiter-admin")
    @PreAuthorize("hasRole('RECRUITER_ADMIN')")
    public ResponseEntity<RecruiterAdminDashboardDTO> getRecruiterAdminDashboard() {
        Long companyId = getCurrentUsersCompanyId();
        return ResponseEntity.ok(recruiterAdminService.getDashboard(companyId));
    }

    @Operation(
            summary = "Get recruiter dashboard",
            description = "Fetch dashboard statistics and summary for logged-in recruiter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recruiter dashboard fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/recruiter")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<RecruiterDashboardDTO> getRecruiterDashboard() {
        Long recruiterId = SecurityUtils.getUserId();
        return ResponseEntity.ok(recruiterService.getDashboard(recruiterId));
    }

    private Long getCurrentUsersCompanyId() {
        Long userId = SecurityUtils.getUserId();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Company company = currentUser.getCompany();
        if (company == null) {
            throw new RuntimeException("Authenticated user does not belong to any company");
        }
        return company.getId();
    }
}