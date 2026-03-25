package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.project.ProjectDTO;
import com.lwd.jobportal.entity.JobSeekerProject;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerProjectRepository;
import com.lwd.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSeekerProjectService {

    private final JobSeekerProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectDTO createProject(Long userId, ProjectDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobSeekerProject project = JobSeekerProject.builder()
                .user(user)
                .projectTitle(dto.getProjectTitle())
                .description(dto.getDescription())
                .technologiesUsed(dto.getTechnologiesUsed())
                .projectUrl(dto.getProjectUrl())
                .githubUrl(dto.getGithubUrl())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .teamSize(dto.getTeamSize())
                .role(dto.getRole())
                .build();

        JobSeekerProject saved = projectRepository.save(project);

        return mapToDTO(saved);
    }


    public ProjectDTO updateProject(Long id, ProjectDTO dto) {

        JobSeekerProject existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        existing.setProjectTitle(dto.getProjectTitle());
        existing.setDescription(dto.getDescription());
        existing.setTechnologiesUsed(dto.getTechnologiesUsed());
        existing.setProjectUrl(dto.getProjectUrl());
        existing.setGithubUrl(dto.getGithubUrl());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setTeamSize(dto.getTeamSize());
        existing.setRole(dto.getRole());

        JobSeekerProject updated = projectRepository.save(existing);

        return mapToDTO(updated);
    }

    
    public List<ProjectDTO> getMe(Long userId) {

        return projectRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    
    public List<ProjectDTO> getByUserId(Long userId) {

        return projectRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ProjectDTO mapToDTO(JobSeekerProject project) {

        return ProjectDTO.builder()
                .id(project.getId())
                .userId(project.getUser().getId())
                .projectTitle(project.getProjectTitle())
                .description(project.getDescription())
                .technologiesUsed(project.getTechnologiesUsed())
                .projectUrl(project.getProjectUrl())
                .githubUrl(project.getGithubUrl())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .teamSize(project.getTeamSize())
                .role(project.getRole())
                .build();
    }
}
