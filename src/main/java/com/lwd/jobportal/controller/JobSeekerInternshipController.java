package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.Internship.InternshipDTO;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobSeekerInternshipService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class JobSeekerInternshipController {

    private final JobSeekerInternshipService internshipService;

    // CREATE INTERNSHIP
    @PostMapping("")
    public ResponseEntity<InternshipDTO> createInternship(
            @RequestBody InternshipDTO dto) {

    	Long userId = SecurityUtils.getUserId();
        InternshipDTO created = internshipService.createInternship(userId, dto);

        return ResponseEntity.ok(created);
    }

    // UPDATE INTERNSHIP
    @PutMapping("/{id}")
    public ResponseEntity<InternshipDTO> updateInternship(
            @PathVariable Long id,
            @RequestBody InternshipDTO dto) {

        InternshipDTO updated = internshipService.updateInternship(id, dto);

        return ResponseEntity.ok(updated);
    }

    // GET MY INTERNSHIPS
    @GetMapping("/me")
    public ResponseEntity<List<InternshipDTO>> getMe() {
    	
    	Long userId = SecurityUtils.getUserId();

        List<InternshipDTO> internships = internshipService.getMe(userId);

        return ResponseEntity.ok(internships);
    }

    // GET INTERNSHIPS BY USER ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InternshipDTO>> getByUserId(
            @PathVariable Long userId) {

        List<InternshipDTO> internships = internshipService.getByUserId(userId);

        return ResponseEntity.ok(internships);
    }
}
