package com.lwd.jobportal.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lwd.jobportal.dto.admin.CompanyAdminDTO;
import com.lwd.jobportal.dto.admin.JobAdminDTO;
import com.lwd.jobportal.dto.admin.UserAdminDTO;
import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterResponse;
import com.lwd.jobportal.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ================= USERS =================
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserAdminDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }


    @PatchMapping("/users/{id}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
        return ResponseEntity.ok("User blocked");	
    }

    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ResponseEntity.ok("User unblocked");
    }
    
    
    // ================= GET RECRUITERS BY COMPANY ID =================
    @GetMapping("/company/{companyId}/recruiters")
    public ResponseEntity<PagedResponse<RecruiterResponse>> getRecruitersByCompanyId(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                adminService.getRecruitersByCompanyId(companyId, page, size)
        );
    }


    // ================= COMPANIES =================
    @GetMapping("/companies")
    public ResponseEntity<PagedResponse<CompanyAdminDTO>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.getAllCompanies(page, size));
    }


    @PatchMapping("/companies/{id}/block")
    public ResponseEntity<String> blockCompany(@PathVariable Long id) {
        adminService.blockCompany(id);
        return ResponseEntity.ok("Company blocked");
    }

    @PatchMapping("/companies/{id}/unblock")
    public ResponseEntity<String> unblockCompany(@PathVariable Long id) {
        adminService.unblockCompany(id);
        return ResponseEntity.ok("Company unblocked");
    }


    // ================= JOBS =================
    @GetMapping("/jobs")
    public ResponseEntity<PagedResponse<JobAdminDTO>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.getAllJobs(page, size));
    }


    @PatchMapping("/jobs/{id}/close")
    public ResponseEntity<String> closeJob(@PathVariable Long id) {
        adminService.closeJob(id);
        return ResponseEntity.ok("Job closed");
    }
}
