package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.cretification.CertificationDTO;
import com.lwd.jobportal.entity.JobSeekerCertification;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerCertificationRepository;
import com.lwd.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSeekerCertificationService {

    private final JobSeekerCertificationRepository certificationRepository;
    private final UserRepository userRepository;


    public CertificationDTO createCertification(Long userId, CertificationDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobSeekerCertification certification = JobSeekerCertification.builder()
                .user(user)
                .certificateName(dto.getCertificateName())
                .issuingOrganization(dto.getIssuingOrganization())
                .issueDate(dto.getIssueDate())
                .expiryDate(dto.getExpiryDate())
                .credentialId(dto.getCredentialId())
                .credentialUrl(dto.getCredentialUrl())
                .skillTag(dto.getSkillTag())
                .certificateFile(dto.getCertificateFile())
                .build();

        JobSeekerCertification saved = certificationRepository.save(certification);

        return mapToDTO(saved);
    }

   
    public CertificationDTO updateCertification(Long id, CertificationDTO dto) {

        JobSeekerCertification existing = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));

        existing.setCertificateName(dto.getCertificateName());
        existing.setIssuingOrganization(dto.getIssuingOrganization());
        existing.setIssueDate(dto.getIssueDate());
        existing.setExpiryDate(dto.getExpiryDate());
        existing.setCredentialId(dto.getCredentialId());
        existing.setCredentialUrl(dto.getCredentialUrl());
        existing.setSkillTag(dto.getSkillTag());
        existing.setCertificateFile(dto.getCertificateFile());

        JobSeekerCertification updated = certificationRepository.save(existing);

        return mapToDTO(updated);
    }

   
    public List<CertificationDTO> getMe(Long userId) {

        return certificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

  
    public List<CertificationDTO> getByUserId(Long userId) {

        return certificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CertificationDTO mapToDTO(JobSeekerCertification certification) {

        return CertificationDTO.builder()
                .id(certification.getId())
                .userId(certification.getUser().getId())
                .certificateName(certification.getCertificateName())
                .issuingOrganization(certification.getIssuingOrganization())
                .issueDate(certification.getIssueDate())
                .expiryDate(certification.getExpiryDate())
                .credentialId(certification.getCredentialId())
                .credentialUrl(certification.getCredentialUrl())
                .skillTag(certification.getSkillTag())
                .certificateFile(certification.getCertificateFile())
                .build();
    }
}
