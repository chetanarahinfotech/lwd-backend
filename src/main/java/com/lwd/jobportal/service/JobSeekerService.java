package com.lwd.jobportal.service;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.repository.JobSeekerRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobSeekerService {

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;

    // ===============================
    // JOB SEEKER METHODS
    // ===============================

    public JobSeekerResponseDTO createOrUpdateProfile(JobSeekerRequestDTO dto) {

        if (!SecurityUtils.hasRole(Role.JOB_SEEKER)) {
            throw new RuntimeException("Access Denied");
        }

        User user = userRepository.findById(SecurityUtils.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobSeeker jobSeeker = mapToEntity(dto);
        jobSeeker.setUser(user);

        // Auto update immediate joiner logic
        if (jobSeeker.getLastWorkingDay() != null &&
                jobSeeker.getLastWorkingDay().isBefore(LocalDate.now())) {

            jobSeeker.setNoticeStatus(NoticeStatus.IMMEDIATE_JOINER);
        }

        JobSeeker saved = jobSeekerRepository.save(jobSeeker);

        return mapToDTO(saved);
    }

    public JobSeekerResponseDTO getMyProfile() {

        Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToDTO(jobSeeker);
    }

    // ===============================
    // RECRUITER METHODS
    // ===============================

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
            throw new RuntimeException("Access Denied");
        }
    }

    // ===============================
    // MAPPER METHODS (INSIDE SERVICE)
    // ===============================

    private JobSeeker mapToEntity(JobSeekerRequestDTO dto) {
        return JobSeeker.builder()
                .noticeStatus(dto.getNoticeStatus())
                .isServingNotice(dto.getIsServingNotice())
                .lastWorkingDay(dto.getLastWorkingDay())
                .noticePeriod(dto.getNoticePeriod())
                .availableFrom(dto.getAvailableFrom())
                .immediateJoiner(dto.getImmediateJoiner())
                .currentCompany(dto.getCurrentCompany())
                .currentCTC(dto.getCurrentCTC())
                .expectedCTC(dto.getExpectedCTC())
                .currentLocation(dto.getCurrentLocation())
                .preferredLocation(dto.getPreferredLocation())
                .totalExperience(dto.getTotalExperience())
                .skills(dto.getSkills())
                .resumeUrl(dto.getResumeUrl())
                .build();
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
