package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.comman.PaginationUtil;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchRequest;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchResponse;
import com.lwd.jobportal.dto.jobseekerdto.SkillResponseDTO;
import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.entity.Skill;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerRepository;
import com.lwd.jobportal.repository.SkillRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JobSeekerService {

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;


    // =====================================================
    // CREATE UPDATE PROFILE
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

    // =====================================================
    // GET PROFILE
    // =====================================================
    
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
    
    
    
    // =====================================================
    // GET PROFILE BY ID
    // =====================================================
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
    // ADD & UPDATE SKILLS
    // =====================================================
    
    @Transactional
    public void updateMySkills(List<String> skillNames) {
    	 Long userId = SecurityUtils.getUserId(); 
    	 
    	 JobSeeker jobSeeker = jobSeekerRepository
    	            .findByUserId(userId)   // ✅ correct method
    	            .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Clear skills if empty
        if (skillNames == null || skillNames.isEmpty()) {
            jobSeeker.getSkills().clear();
            return;
        }

        // =====================================================
        // 1️⃣ Normalize + Remove duplicates
        // =====================================================
        Set<String> normalizedNames = skillNames.stream()
                .filter(Objects::nonNull)
                .map(name -> name.trim().toLowerCase())
                .filter(name -> !name.isBlank())
                .collect(Collectors.toSet());

        // =====================================================
        // 2️⃣ Fetch existing skills (ONE QUERY)
        // =====================================================
        List<Skill> existingSkills =
                skillRepository.findExistingSkills(normalizedNames);

        Map<String, Skill> existingMap = existingSkills.stream()
                .collect(Collectors.toMap(
                        skill -> skill.getName().toLowerCase(),
                        skill -> skill
                ));

        // =====================================================
        // 3️⃣ Create missing skills (Batch insert)
        // =====================================================
        List<Skill> newSkills = new ArrayList<>();

        for (String name : normalizedNames) {
            if (!existingMap.containsKey(name)) {
                newSkills.add(
                        Skill.builder()
                                .name(name)
                                .build()
                );
            }
        }

        if (!newSkills.isEmpty()) {
            try {
                skillRepository.saveAll(newSkills);
                existingSkills.addAll(newSkills);
            } catch (Exception e) {
                // ⚠ In case of race condition (two users insert same skill)
                // Fetch again safely
                existingSkills = skillRepository.findExistingSkills(normalizedNames);
            }
        }

        // =====================================================
        // 4️⃣ Attach unique skills
        // =====================================================
        jobSeeker.getSkills().clear();
        jobSeeker.getSkills().addAll(existingSkills);
    }

    
    @Transactional(readOnly = true)
    public Set<String> getMySkills() {
        Long userId = SecurityUtils.getUserId();
        return skillRepository.findSkillNamesByUserId(userId);
    }
    
    
    public PagedResponse<SkillResponseDTO> getAllSkills(
            String keyword,
            Integer page,
            Integer size
    ) {

        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 10 : size,
                Sort.by("name").ascending()
        );

        Page<Skill> skillPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            skillPage = skillRepository
                    .findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            skillPage = skillRepository.findAll(pageable);
        }

        List<SkillResponseDTO> content = skillPage
                .stream()
                .map(skill -> SkillResponseDTO.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .build())
                .toList();

        return PaginationUtil.buildPagedResponse(skillPage, content);
    }




    
    // =====================================================
    // ABOUT SECTION
    // =====================================================
    
    // =====================================================
    // JOB SEEKER HEADING
    // =====================================================
    
    
    // =====================================================
    // SEARCH JOBSEEKERS
    // =====================================================
    
	public PagedResponse<JobSeekerSearchResponse> searchJobSeekers(
	        JobSeekerSearchRequest request
	) {
	
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
	
	    Sort.Direction direction =
	            request.getSortDirection() != null
	                    ? Sort.Direction.fromString(request.getSortDirection())
	                    : Sort.Direction.DESC;
	
	    String sortBy =
	            request.getSortBy() != null
	                    ? request.getSortBy()
	                    : "totalExperience";
	
	    Pageable pageable = PageRequest.of(
	            request.getPage() != null ? request.getPage() : 0,
	            request.getSize() != null ? request.getSize() : 10,
	            Sort.by(direction, sortBy)
	    );
	
	    Page<JobSeeker> jobSeekerPage =
	            jobSeekerRepository.findAll(specification, pageable);
	
	    List<JobSeekerSearchResponse> content =
	            jobSeekerPage.stream()
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
    
    
    // =====================================================
    // MAPPINGS METHODS
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
