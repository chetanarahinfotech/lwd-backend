package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.jobseekerdto.AboutInfoDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerProfileSummaryResponse;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchRequest;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchResponse;
import com.lwd.jobportal.dto.jobseekerdto.ProfileCompletionDTO;
import com.lwd.jobportal.dto.jobseekerdto.SkillResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.SocialLinksDTO;
import com.lwd.jobportal.dto.jobseekerdto.UpdateSkillsRequest;
import com.lwd.jobportal.security.SecurityUtils;
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
    
    
    @GetMapping("/skills/{userId}")
    public ResponseEntity<Set<String>> getSkillsById(@PathVariable Long userId ) {
    	return ResponseEntity.ok(jobSeekerService.getSkillsById(userId));
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
    
    
	 // =====================================================
	 // 🔹 ABOUT PROFILE SECTION
	 // =====================================================
	
    @PutMapping("/me/about")
    public ResponseEntity<AboutInfoDTO> updateAboutInfo(
            @RequestBody AboutInfoDTO dto) {

        AboutInfoDTO updated = jobSeekerService.updateAboutInfo(dto);
        return ResponseEntity.ok(updated);
    }

	
	
	 @GetMapping("/me/about")
	 public ResponseEntity<AboutInfoDTO> getMyAboutInfo() {
	
	     return ResponseEntity.ok(
	             jobSeekerService.getMyAboutInfo());
	 }
	 
	 @GetMapping("/user/{userId}/about")
	 public ResponseEntity<AboutInfoDTO> getAboutInfoByUserId(
	         @PathVariable Long userId) {
	
	     return ResponseEntity.ok(
	             jobSeekerService.getAboutInfoByUserId(userId));
	 }
	
	 // =====================================================
	 // 🔹 SOCIAL LINKS SECTION
	 // =====================================================
	
	 @PutMapping("/me/social")
	 public ResponseEntity<String> updateSocialLinks(
	         @RequestBody SocialLinksDTO dto) {
	
	     jobSeekerService.updateSocialLinks(dto);
	     return ResponseEntity.ok("Social links updated successfully");
	 }
	
	 @GetMapping("/me/social")
	 public ResponseEntity<SocialLinksDTO> getMySocialLinks() {
	
	     return ResponseEntity.ok(
	             jobSeekerService.getMySocialLinks());
	 }
	 
	 @GetMapping("/user/{userId}/social")
	 public ResponseEntity<SocialLinksDTO> getSocialLinksByUserId(
	         @PathVariable Long userId) {
	
	     return ResponseEntity.ok(
	             jobSeekerService.getSocialLinksByUserId(userId));
	 }
	
	
	
	 // =====================================================
	 // 🔹 PROFILE SUMMARY
	 // =====================================================
	
	 @GetMapping("/me/summary")
	 public ResponseEntity<JobSeekerProfileSummaryResponse> getMyProfileSummary() {
	
	     return ResponseEntity.ok(
	             jobSeekerService.getMyProfileSummary());
	 }
	 
	 @GetMapping("/me/profile-completion")
	 public ResponseEntity<ProfileCompletionDTO> getProfileCompletion() {
		 
		 Long userId = SecurityUtils.getUserId();

	     return ResponseEntity.ok(
	             jobSeekerService.calculateProfileCompletion(userId)
	     );
	 }

	 
	 @GetMapping("/{userId}/profile-completion")
	 public ResponseEntity<ProfileCompletionDTO> getProfileCompletionByUserId(
			 @PathVariable Long userId
			 ) {

	     return ResponseEntity.ok(
	             jobSeekerService.calculateProfileCompletion(userId)
	     );
	 }



    
    
	// =====================================================
	// 🔹 JOB SEEKERS
	// =====================================================

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
