package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.comman.PaginationUtil;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchRequest;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchResponse;
import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.specification.JobSeekerSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    
    
    
    
    public PagedResponse<JobSeekerSearchResponse> searchJobSeekers(
            JobSeekerSearchRequest request
    ) {

//        validateRecruiterAccess();

        Specification<JobSeeker> specification =
                JobSeekerSpecification.searchJobSeekers(
                        request.getKeyword(),
                        request.getSkills(),
                        request.getCurrentLocation(),
                        request.getPreferredLocation(),
                        request.getMinExperience(),
                        request.getMaxExperience(),
                        request.getMinExpectedCTC(),
                        request.getMaxExpectedCTC(),
                        request.getNoticeStatus(),
                        request.getMaxNoticePeriod(),
                        request.getImmediateJoiner(),
                        request.getAvailableBefore()
                );

        Sort sort = Sort.by(
                Sort.Direction.fromString(
                        request.getSortDirection() == null ? "DESC" : request.getSortDirection()
                ),
                request.getSortBy() == null ? "totalExperience" : request.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                request.getPage() == null ? 0 : request.getPage(),
                request.getSize() == null ? 10 : request.getSize(),
                sort
        );

        Page<JobSeeker> jobSeekerPage =
                jobSeekerRepository.findAll(specification, pageable);

        List<JobSeekerSearchResponse> content =
                jobSeekerPage.getContent()
                        .stream()
                        .map(this::toSearchResponse)
                        .toList();

        return PaginationUtil.buildPagedResponse(jobSeekerPage, content);
    }



    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================
    
//    private void validateRecruiterAccess() {
//        if (!(SecurityUtils.hasRole(Role.ADMIN) ||
//        		SecurityUtils.hasRole(Role.RECRUITER) ||
//        		SecurityUtils.hasRole(Role.RECRUITER_ADMIN))) {
//            throw new AccessDeniedException("Only Recruiters can access this resource");
//        }
//    }

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
                .resumeUrl(entity.getResumeUrl())
                .build();
    }
    
   
    private JobSeekerSearchResponse toSearchResponse(JobSeeker jobSeeker) {

        return JobSeekerSearchResponse.builder()
                .id(jobSeeker.getId())
                .fullName(jobSeeker.getUser().getName())
                .email(jobSeeker.getUser().getEmail())
                .currentCompany(jobSeeker.getCurrentCompany())
                .totalExperience(jobSeeker.getTotalExperience())
                .expectedCTC(jobSeeker.getExpectedCTC())
                .currentLocation(jobSeeker.getCurrentLocation())
                .immediateJoiner(jobSeeker.getImmediateJoiner())
                .noticePeriod(jobSeeker.getNoticePeriod())
                .skills(
                        jobSeeker.getSkills()
                                .stream()
                                .map(skill -> skill.getName())
                                .collect(Collectors.toList())
                )
                .build();
    }


}
