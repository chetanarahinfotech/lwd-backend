package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.education.EducationDTO;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobSeekerEducationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/education")
@RequiredArgsConstructor
public class JobSeekerEducationController {

    private final JobSeekerEducationService educationService;
    
    // ================= CREATE EDUCATION =================

    @PostMapping
    public ResponseEntity<EducationDTO> createEducation(@RequestBody EducationDTO dto) {

        Long userId = SecurityUtils.getUserId();

        EducationDTO created = educationService.createEducation(userId, dto);

        return ResponseEntity.ok(created);
    }

    // Get all education of a user
    @GetMapping("/me")
    public ResponseEntity<List<EducationDTO>> getMyEducation() {
    	
    	Long userId = SecurityUtils.getUserId();

        List<EducationDTO> educationList = educationService.getMyEducation(userId);

        return ResponseEntity.ok(educationList);
    }

    // Get education by ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EducationDTO>> getEducationByUserId(@PathVariable Long userId) {

        List<EducationDTO> education = educationService.getEducationByUserId(userId);

        return ResponseEntity.ok(education);
    }

    // Update education
    @PutMapping("/{educationId}")
    public ResponseEntity<EducationDTO> updateEducation(
            @PathVariable Long educationId,
            @RequestBody EducationDTO dto) {

        EducationDTO updatedEducation = educationService.updateEducation(educationId, dto);

        return ResponseEntity.ok(updatedEducation);
    }
}
