package com.lwd.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.cretification.CertificationDTO;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.JobSeekerCertificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class JobSeekerCertificationController {

    private final JobSeekerCertificationService certificationService;

    // CREATE CERTIFICATION
    @PostMapping
    public ResponseEntity<CertificationDTO> createCertification(
            @RequestBody CertificationDTO dto) {

        Long userId = SecurityUtils.getUserId();

        CertificationDTO created = certificationService.createCertification(userId, dto);

        return ResponseEntity.status(201).body(created);
    }


    // UPDATE CERTIFICATION
    @PutMapping("/{id}")
    public ResponseEntity<CertificationDTO> updateCertification(
            @PathVariable Long id,
            @RequestBody CertificationDTO dto) {

        CertificationDTO updated = certificationService.updateCertification(id, dto);

        return ResponseEntity.ok(updated);
    }

    // GET MY CERTIFICATIONS
    @GetMapping("/me")
    public ResponseEntity<List<CertificationDTO>> getMe() {
    	
    	Long userId = SecurityUtils.getUserId();

        List<CertificationDTO> certifications = certificationService.getMe(userId);

        return ResponseEntity.ok(certifications);
    }

    // GET CERTIFICATIONS BY USER ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CertificationDTO>> getByUserId(
            @PathVariable Long userId) {

        List<CertificationDTO> certifications = certificationService.getByUserId(userId);

        return ResponseEntity.ok(certifications);
    }
}
