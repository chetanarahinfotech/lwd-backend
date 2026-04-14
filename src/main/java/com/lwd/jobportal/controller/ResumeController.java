package com.lwd.jobportal.controller;

import com.lwd.jobportal.dto.resume.ResumeFileResponse;
import com.lwd.jobportal.dto.resume.ResumeResponse;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.ResumeService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "makeDefault", defaultValue = "true") boolean makeDefault
    ) {
        Long userId = SecurityUtils.getUserId();

        ResumeResponse response = resumeService.uploadResume(userId, file, makeDefault);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Resume uploaded successfully",
                "data", response
        ));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<ResumeResponse>> getMyResumes() {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(resumeService.getMyResumes(userId));
    }

    @PutMapping("/{resumeId}/default")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Object>> setDefaultResume(@PathVariable Long resumeId) {
        Long userId = SecurityUtils.getUserId();

        ResumeResponse response = resumeService.setDefaultResume(userId, resumeId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Default resume updated successfully",
                "data", response
        ));
    }

    @DeleteMapping("/{resumeId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Object>> deleteResume(@PathVariable Long resumeId) {
        Long userId = SecurityUtils.getUserId();
        resumeService.softDeleteResume(userId, resumeId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Resume deleted successfully"
        ));
    }

    @PostMapping("/{resumeId}/view")
    @PreAuthorize("hasAnyRole('JOB_SEEKER','RECRUITER','RECRUITER_ADMIN','ADMIN')")
    public ResponseEntity<ResumeFileResponse> viewResume(
            @PathVariable Long resumeId,
            @RequestParam String viewSource,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long applicationId,
            HttpServletRequest request
    ) {
        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        ResumeFileResponse response = resumeService.viewResume(
                resumeId,
                userId,
                role,
                request,
                viewSource,
                jobId,
                applicationId
        );

        return ResponseEntity.ok(response);
    }
}