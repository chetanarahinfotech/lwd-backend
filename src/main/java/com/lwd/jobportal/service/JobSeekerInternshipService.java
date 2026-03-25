package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.Internship.InternshipDTO;
import com.lwd.jobportal.entity.JobSeekerInternship;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerInternshipRepository;
import com.lwd.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSeekerInternshipService {

    private final JobSeekerInternshipRepository internshipRepository;
    private final UserRepository userRepository;

    // CREATE
    public InternshipDTO createInternship(Long userId, InternshipDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobSeekerInternship internship = JobSeekerInternship.builder()
                .user(user)
                .companyName(dto.getCompanyName())
                .role(dto.getRole())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .skills(dto.getSkills())
                .stipend(dto.getStipend())
                .employmentType(dto.getEmploymentType())
                .build();

        JobSeekerInternship saved = internshipRepository.save(internship);

        return mapToDTO(saved);
    }

    // UPDATE
    public InternshipDTO updateInternship(Long id, InternshipDTO dto) {

        JobSeekerInternship existing = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));

        existing.setCompanyName(dto.getCompanyName());
        existing.setRole(dto.getRole());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setDescription(dto.getDescription());
        existing.setLocation(dto.getLocation());
        existing.setSkills(dto.getSkills());
        existing.setStipend(dto.getStipend());
        existing.setEmploymentType(dto.getEmploymentType());

        JobSeekerInternship updated = internshipRepository.save(existing);

        return mapToDTO(updated);
    }

    // GET ME
    public List<InternshipDTO> getMe(Long userId) {

        return internshipRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET BY USER ID
    public List<InternshipDTO> getByUserId(Long userId) {

        return internshipRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ENTITY → DTO
    private InternshipDTO mapToDTO(JobSeekerInternship internship) {

        return InternshipDTO.builder()
                .id(internship.getId())
                .userId(internship.getUser().getId())
                .companyName(internship.getCompanyName())
                .role(internship.getRole())
                .startDate(internship.getStartDate())
                .endDate(internship.getEndDate())
                .description(internship.getDescription())
                .location(internship.getLocation())
                .skills(internship.getSkills())
                .stipend(internship.getStipend())
                .employmentType(internship.getEmploymentType())
                .build();
    }
}
