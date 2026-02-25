package com.lwd.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.jobdto.JobResponse;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterResponse;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.RecruiterAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recruiter-admin")
@RequiredArgsConstructor
public class RecruiterAdminController {

    private final RecruiterAdminService recruiterAdminService;

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

    


    // ================= APPROVE RECRUITER =================
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PutMapping("/recruiters/{id}/approve")
    public ResponseEntity<RecruiterResponse> approveRecruiter(@PathVariable Long id) {

        Long adminId = SecurityUtils.getUserId();
        
        System.out.println("Approve request");

        RecruiterResponse response =
                recruiterAdminService.approveRecruiter(id, adminId);

        return ResponseEntity.ok(response);
    }


    // ================= BLOCK / UNBLOCK RECRUITER =================
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PutMapping("/recruiters/{id}/block")
    public ResponseEntity<RecruiterResponse> blockRecruiter(
            @PathVariable Long id,
            @RequestParam boolean block) {

        RecruiterResponse response = recruiterAdminService.blockRecruiter(id, block);
        return ResponseEntity.ok(response);
    }
    
 
    
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/recruiter/{recruiterId}/jobs")
    public ResponseEntity<PagedResponse<JobResponse>> getJobsByRecruiter(
            @PathVariable Long recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                recruiterAdminService.getJobsByRecruiter(recruiterId, page, size)
        );
    }

}
