package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.service.JobSeekerService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-seekers")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;

    // =========================================
    // JOB SEEKER ENDPOINTS (Self Profile)
    // =========================================

    @PostMapping("/profile")
    public JobSeekerResponseDTO createOrUpdateProfile(
            @RequestBody JobSeekerRequestDTO dto) {
        return jobSeekerService.createOrUpdateProfile(dto);
    }

    @GetMapping("/me")
    public JobSeekerResponseDTO getMyProfile() {
        return jobSeekerService.getMyProfile();
    }
    
    @GetMapping("/user/{userId}")
    public JobSeekerResponseDTO getJobSeekerByUserId(
    		@PathVariable Long userId) {
        return jobSeekerService.getJobSeekerByUserId(userId);
    }

    // =========================================
    // RECRUITER ENDPOINTS
    // =========================================

    @PreAuthorize("hasAnyRole('RECRUITER','RECRUITER_ADMIN')")
    @GetMapping("/immediate-joiners")
    public List<JobSeekerResponseDTO> getImmediateJoiners() {
        return jobSeekerService.getImmediateJoiners();
    }

    @PreAuthorize("hasAnyRole('RECRUITER','RECRUITER_ADMIN')")
    @GetMapping("/notice-status")
    public List<JobSeekerResponseDTO> getByNoticeStatus(
            @RequestParam NoticeStatus status) {
        return jobSeekerService.getByNoticeStatus(status);
    }

    @PreAuthorize("hasAnyRole('RECRUITER','RECRUITER_ADMIN')")
    @GetMapping("/lwd-within/{days}")
    public List<JobSeekerResponseDTO> getLwdWithinDays(
            @PathVariable int days) {
        return jobSeekerService.getLwdWithinDays(days);
    }

    @PreAuthorize("hasAnyRole('RECRUITER','RECRUITER_ADMIN')")
    @GetMapping("/search-by-location")
    public List<JobSeekerResponseDTO> searchByLocation(
            @RequestParam String location) {
        return jobSeekerService.searchByLocation(location);
    }
}
