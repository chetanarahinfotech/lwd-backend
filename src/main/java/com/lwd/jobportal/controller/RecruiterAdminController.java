package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.jobdto.JobResponse;
import com.lwd.jobportal.recruiteradmindto.RecruiterResponse;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.RecruiterAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recruiter-admin")
@RequiredArgsConstructor
public class RecruiterAdminController {

    private final RecruiterAdminService recruiterAdminService;

    // ================= LIST ALL RECRUITERS =================
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/recruiters")
    public ResponseEntity<List<RecruiterResponse>> getRecruiters(){
        // The principal is the logged-in user's ID
        Long recruiterAdminId = SecurityUtils.getUserId();
        
        List<RecruiterResponse> recruiters = recruiterAdminService.getCompanyRecruiters(recruiterAdminId);
        
        return ResponseEntity.ok(recruiters);
    }
    
    
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/recruiters/pending")
    public ResponseEntity<List<RecruiterResponse>> getPendingRecruiters() {

        Long adminId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                recruiterAdminService.getPendingRecruiters(adminId)
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
    public ResponseEntity<List<JobResponse>> getJobsByRecruiter(
            @PathVariable Long recruiterId
    ) {
        return ResponseEntity.ok(
                recruiterAdminService.getJobsByRecruiter(recruiterId)
        );
    }
}
