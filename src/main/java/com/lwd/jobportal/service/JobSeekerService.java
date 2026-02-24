package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobSeekerService {

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;

    // =====================================================
    // JOB SEEKER METHODS
    // =====================================================

    public JobSeekerResponseDTO createOrUpdateProfile(JobSeekerRequestDTO dto) {

        if (!SecurityUtils.hasRole(Role.JOB_SEEKER)) {
            throw new AccessDeniedException("Only Job Seekers can update profile");
        }

        Long userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🔥 Fetch existing profile if present
        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElse(null);

        // 🔥 Create new if not exists
        if (jobSeeker == null) {
            jobSeeker = new JobSeeker();
            jobSeeker.setUser(user);
        }

        // 🔥 Update fields
        updateFields(jobSeeker, dto);

        // 🔥 Auto Immediate Joiner Logic
        if (jobSeeker.getLastWorkingDay() != null &&
                jobSeeker.getLastWorkingDay().isBefore(LocalDate.now())) {
            jobSeeker.setNoticeStatus(NoticeStatus.IMMEDIATE_JOINER);
            jobSeeker.setImmediateJoiner(true);
        }

        JobSeeker saved = jobSeekerRepository.save(jobSeeker);

        return mapToDTO(saved);
    }

    public JobSeekerResponseDTO getMyProfile() {

        if (!SecurityUtils.hasRole(Role.JOB_SEEKER)) {
            throw new AccessDeniedException("Only Job Seekers can access profile");
        }

        Long userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElse(null);

        // 🔥 CREATE PROFILE IF NOT EXISTS
        if (jobSeeker == null) {
            jobSeeker = new JobSeeker();
            jobSeeker.setUser(user);

            jobSeeker = jobSeekerRepository.save(jobSeeker);
        }

        return mapToDTO(jobSeeker);
    }
    
    public JobSeekerResponseDTO getJobSeekerByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElse(null);

        // 🔥 CREATE PROFILE IF NOT EXISTS
        if (jobSeeker == null) {
            jobSeeker = new JobSeeker();
            jobSeeker.setUser(user);

            jobSeeker = jobSeekerRepository.save(jobSeeker);
        }

        return mapToDTO(jobSeeker);
    }


    // =====================================================
    // RECRUITER METHODS
    // =====================================================

    public List<JobSeekerResponseDTO> getImmediateJoiners() {
        validateRecruiterAccess();

        return jobSeekerRepository.findByImmediateJoinerTrue()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<JobSeekerResponseDTO> getByNoticeStatus(NoticeStatus status) {
        validateRecruiterAccess();

        return jobSeekerRepository.findByNoticeStatus(status)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<JobSeekerResponseDTO> getLwdWithinDays(int days) {
        validateRecruiterAccess();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        return jobSeekerRepository.findByLastWorkingDayBetween(today, endDate)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<JobSeekerResponseDTO> searchByLocation(String location) {
        validateRecruiterAccess();

        return jobSeekerRepository.findByPreferredLocation(location)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private void validateRecruiterAccess() {
        if (!(SecurityUtils.hasRole(Role.RECRUITER) ||
              SecurityUtils.hasRole(Role.RECRUITER_ADMIN))) {
            throw new AccessDeniedException("Only Recruiters can access this resource");
        }
    }

    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================

    private void updateFields(JobSeeker jobSeeker, JobSeekerRequestDTO dto) {

        jobSeeker.setNoticeStatus(dto.getNoticeStatus());
        jobSeeker.setIsServingNotice(dto.getIsServingNotice());
        jobSeeker.setLastWorkingDay(dto.getLastWorkingDay());
        jobSeeker.setNoticePeriod(dto.getNoticePeriod());
        jobSeeker.setAvailableFrom(dto.getAvailableFrom());
        jobSeeker.setImmediateJoiner(dto.getImmediateJoiner());
        jobSeeker.setCurrentCompany(dto.getCurrentCompany());
        jobSeeker.setCurrentCTC(dto.getCurrentCTC());
        jobSeeker.setExpectedCTC(dto.getExpectedCTC());
        jobSeeker.setCurrentLocation(dto.getCurrentLocation());
        jobSeeker.setPreferredLocation(dto.getPreferredLocation());
        jobSeeker.setTotalExperience(dto.getTotalExperience());
        jobSeeker.setSkills(dto.getSkills());
        jobSeeker.setResumeUrl(dto.getResumeUrl());
    }

    private JobSeekerResponseDTO mapToDTO(JobSeeker entity) {

        return JobSeekerResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .fullName(entity.getUser().getName())
                .email(entity.getUser().getEmail())
                .noticeStatus(entity.getNoticeStatus())
                .isServingNotice(entity.getIsServingNotice())
                .lastWorkingDay(entity.getLastWorkingDay())
                .noticePeriod(entity.getNoticePeriod())
                .availableFrom(entity.getAvailableFrom())
                .immediateJoiner(entity.getImmediateJoiner())
                .currentCompany(entity.getCurrentCompany())
                .currentCTC(entity.getCurrentCTC())
                .expectedCTC(entity.getExpectedCTC())
                .currentLocation(entity.getCurrentLocation())
                .preferredLocation(entity.getPreferredLocation())
                .totalExperience(entity.getTotalExperience())
                .skills(entity.getSkills())
                .resumeUrl(entity.getResumeUrl())
                .build();
    }
}
