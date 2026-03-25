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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-seekers")
@RequiredArgsConstructor
@Tag(name = "Job Seekers", description = "Job seeker profile, skills, social links, profile completion, and search APIs")
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;

    // =========================================
    // JOB SEEKER ENDPOINTS (Self Profile)
    // =========================================

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create or update job seeker profile",
            description = "Create or update the profile of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/profile")
    public JobSeekerResponseDTO createOrUpdateProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job seeker profile request",
                    required = true
            )
            @RequestBody JobSeekerRequestDTO dto) {
        return jobSeekerService.createOrUpdateProfile(dto);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my profile",
            description = "Fetch the profile of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public JobSeekerResponseDTO getMyProfile() {
        return jobSeekerService.getMyProfile();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my skills",
            description = "Fetch skills of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skills fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/myskills")
    public ResponseEntity<Set<String>> getMySkills() {
        return ResponseEntity.ok(jobSeekerService.getMySkills());
    }

    @Operation(
            summary = "Get skills by user ID",
            description = "Fetch skills for a job seeker by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skills fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/skills/{userId}")
    public ResponseEntity<Set<String>> getSkillsById(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        return ResponseEntity.ok(jobSeekerService.getSkillsById(userId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update my skills",
            description = "Update skills of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skills updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/updateskills")
    public ResponseEntity<?> updateMySkills(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Skill update request",
                    required = true
            )
            @RequestBody UpdateSkillsRequest request
    ) {
        jobSeekerService.updateMySkills(request.getSkills());
        return ResponseEntity.ok("Skills updated successfully");
    }

    @Operation(
            summary = "Get all skills",
            description = "Fetch paginated list of skills with optional keyword filter"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skills fetched successfully")
    })
    @Parameters({
            @Parameter(name = "keyword", description = "Skill keyword filter"),
            @Parameter(name = "page", description = "Page number"),
            @Parameter(name = "size", description = "Page size")
    })
    @GetMapping("/skills")
    public PagedResponse<SkillResponseDTO> getSkills(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return jobSeekerService.getAllSkills(keyword, page, size);
    }

    @Operation(
            summary = "Get job seeker by user ID",
            description = "Fetch job seeker profile by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job seeker fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Job seeker not found")
    })
    @GetMapping("/user/{userId}")
    public JobSeekerResponseDTO getJobSeekerByUserId(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        return jobSeekerService.getJobSeekerByUserId(userId);
    }

    // =====================================================
    // ABOUT PROFILE SECTION
    // =====================================================

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update about information",
            description = "Update about section of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "About info updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me/about")
    public ResponseEntity<AboutInfoDTO> updateAboutInfo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "About information request",
                    required = true
            )
            @RequestBody AboutInfoDTO dto) {

        AboutInfoDTO updated = jobSeekerService.updateAboutInfo(dto);
        return ResponseEntity.ok(updated);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my about information",
            description = "Fetch about section of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "About info fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me/about")
    public ResponseEntity<AboutInfoDTO> getMyAboutInfo() {
        return ResponseEntity.ok(jobSeekerService.getMyAboutInfo());
    }

    @Operation(
            summary = "Get about information by user ID",
            description = "Fetch about section for a job seeker by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "About info fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/about")
    public ResponseEntity<AboutInfoDTO> getAboutInfoByUserId(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        return ResponseEntity.ok(jobSeekerService.getAboutInfoByUserId(userId));
    }

    // =====================================================
    // SOCIAL LINKS SECTION
    // =====================================================

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update social links",
            description = "Update social links of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Social links updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me/social")
    public ResponseEntity<String> updateSocialLinks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Social links request",
                    required = true
            )
            @RequestBody SocialLinksDTO dto) {

        jobSeekerService.updateSocialLinks(dto);
        return ResponseEntity.ok("Social links updated successfully");
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my social links",
            description = "Fetch social links of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Social links fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me/social")
    public ResponseEntity<SocialLinksDTO> getMySocialLinks() {
        return ResponseEntity.ok(jobSeekerService.getMySocialLinks());
    }

    @Operation(
            summary = "Get social links by user ID",
            description = "Fetch social links for a job seeker by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Social links fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/social")
    public ResponseEntity<SocialLinksDTO> getSocialLinksByUserId(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        return ResponseEntity.ok(jobSeekerService.getSocialLinksByUserId(userId));
    }

    // =====================================================
    // PROFILE SUMMARY
    // =====================================================

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my profile summary",
            description = "Fetch profile summary of the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile summary fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me/summary")
    public ResponseEntity<JobSeekerProfileSummaryResponse> getMyProfileSummary() {
        return ResponseEntity.ok(jobSeekerService.getMyProfileSummary());
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my profile completion",
            description = "Calculate profile completion percentage for the logged-in job seeker"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile completion fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me/profile-completion")
    public ResponseEntity<ProfileCompletionDTO> getProfileCompletion() {

        Long userId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                jobSeekerService.calculateProfileCompletion(userId)
        );
    }

    @Operation(
            summary = "Get profile completion by user ID",
            description = "Calculate profile completion percentage for a job seeker by user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile completion fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/profile-completion")
    public ResponseEntity<ProfileCompletionDTO> getProfileCompletionByUserId(
            @Parameter(description = "User ID")
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                jobSeekerService.calculateProfileCompletion(userId)
        );
    }

    // =====================================================
    // JOB SEEKERS SEARCH
    // =====================================================

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Search job seekers",
            description = "Search job seekers with filters. Accessible to ADMIN, RECRUITER, and RECRUITER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job seekers fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER','RECRUITER_ADMIN')")
    @PostMapping("/search")
    public ResponseEntity<PagedResponse<JobSeekerSearchResponse>> searchJobSeekers(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job seeker search request",
                    required = true
            )
            @RequestBody JobSeekerSearchRequest request
    ) {

        PagedResponse<JobSeekerSearchResponse> response =
                jobSeekerService.searchJobSeekers(request);

        return ResponseEntity.ok(response);
    }
}