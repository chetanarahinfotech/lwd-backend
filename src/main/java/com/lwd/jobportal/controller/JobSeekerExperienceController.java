package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.experience.ExperienceDTO;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobSeekerExperienceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/experience")
@RequiredArgsConstructor
public class JobSeekerExperienceController {

    private final JobSeekerExperienceService experienceService;

    /* ================= GET MY EXPERIENCE ================= */

    @GetMapping("/me")
    public ResponseEntity<List<ExperienceDTO>> getMyExperience() {

        Long userId = SecurityUtils.getUserId();

        List<ExperienceDTO> experiences =
                experienceService.getMyExperience(userId);

        return ResponseEntity.ok(experiences);
    }

    /* ================= GET EXPERIENCE BY ID ================= */

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExperienceDTO>> getExperienceByUserId(
            @PathVariable Long userId) {

        List<ExperienceDTO> experiences =
                experienceService.getExperienceByUserId(userId);

        return ResponseEntity.ok(experiences);
    }

    /* ================= CREATE EXPERIENCE ================= */

    @PostMapping
    public ResponseEntity<ExperienceDTO> createExperience(
            @RequestBody ExperienceDTO dto) {

        Long userId = SecurityUtils.getUserId();
        dto.setUserId(userId);

        ExperienceDTO createdExperience =
                experienceService.createExperience(dto);

        return ResponseEntity.ok(createdExperience);
    }

    /* ================= UPDATE EXPERIENCE ================= */

    @PutMapping("/{experienceId}")
    public ResponseEntity<ExperienceDTO> updateExperience(
            @PathVariable Long experienceId,
            @RequestBody ExperienceDTO dto) {

        Long userId = SecurityUtils.getUserId();
        dto.setUserId(userId);

        ExperienceDTO updatedExperience =
                experienceService.updateExperience(experienceId, dto);

        return ResponseEntity.ok(updatedExperience);
    }
}
