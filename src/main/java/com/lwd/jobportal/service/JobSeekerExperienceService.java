package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.experience.ExperienceDTO;
import com.lwd.jobportal.entity.JobSeekerExperience;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerExperienceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSeekerExperienceService {

    private final JobSeekerExperienceRepository experienceRepository;

    /* ================= GET MY EXPERIENCE ================= */

    public List<ExperienceDTO> getMyExperience(Long userId) {

        List<JobSeekerExperience> experiences =
                experienceRepository.findByUserId(userId);

        return experiences.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExperienceDTO> getExperienceByUserId(Long userId) {

        List<JobSeekerExperience> experiences =
                experienceRepository.findByUserId(userId);

        return experiences.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /* ================= CREATE EXPERIENCE ================= */

    public ExperienceDTO createExperience(ExperienceDTO dto) {

        User user = new User();
        user.setId(dto.getUserId());

        JobSeekerExperience experience = toEntity(dto, user);

        JobSeekerExperience saved =
                experienceRepository.save(experience);

        return toDTO(saved);
    }

    /* ================= UPDATE EXPERIENCE ================= */

    public ExperienceDTO updateExperience(Long experienceId, ExperienceDTO dto) {

        JobSeekerExperience experience = experienceRepository
                .findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));

        experience.setCompanyName(dto.getCompanyName());
        experience.setJobTitle(dto.getJobTitle());
        experience.setEmploymentType(dto.getEmploymentType());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setCurrentlyWorking(dto.getCurrentlyWorking());
        experience.setJobDescription(dto.getJobDescription());
        experience.setLocation(dto.getLocation());

        JobSeekerExperience updated =
                experienceRepository.save(experience);

        return toDTO(updated);
    }

    /* ================= ENTITY → DTO ================= */

    private ExperienceDTO toDTO(JobSeekerExperience exp) {

        return ExperienceDTO.builder()
                .id(exp.getId())
                .userId(exp.getUser().getId())
                .companyName(exp.getCompanyName())
                .jobTitle(exp.getJobTitle())
                .employmentType(exp.getEmploymentType())
                .startDate(exp.getStartDate())
                .endDate(exp.getEndDate())
                .currentlyWorking(exp.getCurrentlyWorking())
                .jobDescription(exp.getJobDescription())
                .location(exp.getLocation())
                .build();
    }

    /* ================= DTO → ENTITY ================= */

    private JobSeekerExperience toEntity(ExperienceDTO dto, User user) {

        return JobSeekerExperience.builder()
                .user(user)
                .companyName(dto.getCompanyName())
                .jobTitle(dto.getJobTitle())
                .employmentType(dto.getEmploymentType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .currentlyWorking(dto.getCurrentlyWorking())
                .jobDescription(dto.getJobDescription())
                .location(dto.getLocation())
                .build();
    }
}
