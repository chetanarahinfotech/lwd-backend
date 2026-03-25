package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.education.EducationDTO;
import com.lwd.jobportal.entity.JobSeekerEducation;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerEducationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSeekerEducationService {

    private final JobSeekerEducationRepository educationRepository;
    
    
    // ================= CREATE EDUCATION =================

    public EducationDTO createEducation(Long userId, EducationDTO dto) {

        User user = new User();
        user.setId(userId);

        JobSeekerEducation education = toEntity(dto, user);

        JobSeekerEducation savedEducation = educationRepository.save(education);

        return toDTO(savedEducation);
    }

    // Get all education of a user
    public List<EducationDTO> getMyEducation(Long userId) {
        return educationRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EducationDTO> getEducationByUserId(Long userId) {

        return educationRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }
    
    
    public EducationDTO updateEducation(Long educationId, EducationDTO dto) {

        JobSeekerEducation education = educationRepository
                .findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));

        // If education exists → update
        if (education != null) {

            education.setDegree(dto.getDegree());
            education.setFieldOfStudy(dto.getFieldOfStudy());
            education.setInstitutionName(dto.getInstitutionName());
            education.setUniversity(dto.getUniversity());
            education.setStartDate(dto.getStartDate());
            education.setEndDate(dto.getEndDate());
            education.setPercentage(dto.getPercentage());
            education.setGrade(dto.getGrade());

        } else {

            // If not exists → create new
            User user = new User();
            user.setId(dto.getUserId());

            education = toEntity(dto, user);
        }

        JobSeekerEducation savedEducation = educationRepository.save(education);

        return toDTO(savedEducation);
    }



    // Entity → DTO
    public EducationDTO toDTO(JobSeekerEducation education) {
        return EducationDTO.builder()
                .id(education.getId())
                .userId(education.getUser().getId())
                .degree(education.getDegree())
                .fieldOfStudy(education.getFieldOfStudy())
                .institutionName(education.getInstitutionName())
                .university(education.getUniversity())
                .startDate(education.getStartDate())
                .endDate(education.getEndDate())
                .percentage(education.getPercentage())
                .grade(education.getGrade())
                .build();
    }

    // DTO → Entity (ID removed because it is auto-generated)
    public JobSeekerEducation toEntity(EducationDTO dto, User user) {
        return JobSeekerEducation.builder()
                .user(user)
                .degree(dto.getDegree())
                .fieldOfStudy(dto.getFieldOfStudy())
                .institutionName(dto.getInstitutionName())
                .university(dto.getUniversity())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .percentage(dto.getPercentage())
                .grade(dto.getGrade())
                .build();
    }
}
