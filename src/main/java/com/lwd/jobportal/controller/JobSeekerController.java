package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchRequest;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchResponse;
import com.lwd.jobportal.dto.jobseekerdto.SkillResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.UpdateSkillsRequest;
import com.lwd.jobportal.service.JobSeekerService;

import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


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
    
    @GetMapping("/myskills")
    public ResponseEntity<Set<String>> getMySkills() {
    	return ResponseEntity.ok(jobSeekerService.getMySkills());
    }
    
    @PutMapping("/updateskills")
    public ResponseEntity<?> updateMySkills(
            @RequestBody UpdateSkillsRequest request
    ) {

        jobSeekerService.updateMySkills(request.getSkills());

        return ResponseEntity.ok("Skills updated successfully");
    }
    
    
    @GetMapping("/skills")
    public PagedResponse<SkillResponseDTO> getSkills(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return jobSeekerService.getAllSkills(keyword, page, size);
    }

    
    @GetMapping("/user/{userId}")
    public JobSeekerResponseDTO getJobSeekerByUserId(
    		@PathVariable Long userId) {
        return jobSeekerService.getJobSeekerByUserId(userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER','RECRUITER_ADMIN')")
    @PostMapping("/search")
    public ResponseEntity<PagedResponse<JobSeekerSearchResponse>> searchJobSeekers(
            @RequestBody JobSeekerSearchRequest request
    ) {

        PagedResponse<JobSeekerSearchResponse> response =
                jobSeekerService.searchJobSeekers(request);

        return ResponseEntity.ok(response);
    }

}
