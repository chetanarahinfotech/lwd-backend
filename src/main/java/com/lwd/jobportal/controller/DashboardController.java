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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AdminDashboardService adminService;
    private final RecruiterAdminDashboardService recruiterAdminService;
    private final RecruiterDashboardService recruiterService;
    private final UserRepository userRepository;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    @GetMapping("/recruiter-admin")
    @PreAuthorize("hasRole('RECRUITER_ADMIN')")
    public ResponseEntity<RecruiterAdminDashboardDTO> getRecruiterAdminDashboard() {
        Long companyId = getCurrentUsersCompanyId();
        return ResponseEntity.ok(recruiterAdminService.getDashboard(companyId));
    }

    @GetMapping("/recruiter")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<RecruiterDashboardDTO> getRecruiterDashboard() {
        Long recruiterId = SecurityUtils.getUserId();
        return ResponseEntity.ok(recruiterService.getDashboard(recruiterId));
    }

    // Helper to get the company ID of the currently authenticated recruiter admin
    private Long getCurrentUsersCompanyId() {
    	Long userId = SecurityUtils.getUserId();
    	User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        Company company = currentUser.getCompany();
        if (company == null) {
            throw new RuntimeException("Authenticated user does not belong to any company");
        }
        return company.getId();
    }
}