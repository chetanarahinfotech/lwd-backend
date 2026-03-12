package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.project.ProjectDTO;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobSeekerProjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class JobSeekerProjectController {

    private final JobSeekerProjectService projectService;

    // CREATE PROJECT
    @PostMapping("")
    public ResponseEntity<ProjectDTO> createProject(
            @RequestBody ProjectDTO dto) {
    	
    	Long userId = SecurityUtils.getUserId();

        ProjectDTO created = projectService.createProject(userId, dto);

        return ResponseEntity.ok(created);
    }

    // UPDATE PROJECT
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectDTO dto) {

        ProjectDTO updated = projectService.updateProject(id, dto);

        return ResponseEntity.ok(updated);
    }

    // GET MY PROJECTS
    @GetMapping("/me")
    public ResponseEntity<List<ProjectDTO>> getMe() {

    	Long userId = SecurityUtils.getUserId();
        List<ProjectDTO> projects = projectService.getMe(userId);

        return ResponseEntity.ok(projects);
    }

    // GET PROJECTS BY USER ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectDTO>> getByUserId(
            @PathVariable Long userId) {

        List<ProjectDTO> projects = projectService.getByUserId(userId);

        return ResponseEntity.ok(projects);
    }
}
