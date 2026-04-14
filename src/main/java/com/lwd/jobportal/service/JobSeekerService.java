package com.lwd.jobportal.service;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.comman.PaginationUtil;
import com.lwd.jobportal.dto.jobseekerdto.AboutInfoDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerProfileSummaryResponse;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerRequestDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchRequest;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSearchResponse;
import com.lwd.jobportal.dto.jobseekerdto.ProfileCompletionDTO;
import com.lwd.jobportal.dto.jobseekerdto.SkillResponseDTO;
import com.lwd.jobportal.dto.jobseekerdto.SocialLinksDTO;
import com.lwd.jobportal.dto.resume.ResumeViewCountProjection;
import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.entity.Resume;
import com.lwd.jobportal.entity.Skill;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.JobSeekerCertificationRepository;
import com.lwd.jobportal.repository.JobSeekerEducationRepository;
import com.lwd.jobportal.repository.JobSeekerExperienceRepository;
import com.lwd.jobportal.repository.JobSeekerInternshipRepository;
import com.lwd.jobportal.repository.JobSeekerProjectRepository;
import com.lwd.jobportal.repository.JobSeekerRepository;
import com.lwd.jobportal.repository.ResumeRepository;
import com.lwd.jobportal.repository.ResumeViewHistoryRepository;
import com.lwd.jobportal.repository.SkillRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.searchhistory.dto.SaveSearchHistoryRequest;
import com.lwd.jobportal.searchhistory.enums.SearchModule;
import com.lwd.jobportal.searchhistory.enums.SearchType;
import com.lwd.jobportal.searchhistory.service.SearchHistoryService;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.specification.JobSeekerSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobSeekerService {

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final JobSeekerEducationRepository educationRepository;
    private final JobSeekerExperienceRepository experienceRepository;
    private final JobSeekerInternshipRepository internshipRepository;
    private final JobSeekerCertificationRepository certificationRepository;
    private final JobSeekerProjectRepository projectRepository;
    private final ResumeRepository resumeRepository;
    private final SearchHistoryService searchHistoryService;
    private final ResumeViewHistoryRepository resumeViewHistoryRepository;
    


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
    	            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

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
    
    
    @Transactional(readOnly = true)
    public Set<String> getSkillsById(Long userId) {
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
    
    
    @Transactional
    public AboutInfoDTO updateAboutInfo(AboutInfoDTO dto) {

        Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        jobSeeker.setHeadline(dto.getHeadline());
        jobSeeker.setAbout(dto.getAbout());

        jobSeekerRepository.save(jobSeeker);

        AboutInfoDTO response = new AboutInfoDTO();
        response.setHeadline(jobSeeker.getHeadline());
        response.setAbout(jobSeeker.getAbout());

        return response;
    }


    // ===============================
    // 🔹 Delete Basic Info Section
    // ===============================

    @Transactional
    public void deleteBasicProfile() {
    	
    	Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        jobSeeker.setHeadline(null);
        jobSeeker.setAbout(null);

        jobSeekerRepository.save(jobSeeker);
    }

    // ===============================
    // 🔹 Update Social Links Section
    // ===============================

    @Transactional
    public void updateSocialLinks(SocialLinksDTO dto) {
    	
    	Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        jobSeeker.setLinkedinUrl(dto.getLinkedinUrl());
        jobSeeker.setGithubUrl(dto.getGithubUrl());
        jobSeeker.setPortfolioUrl(dto.getPortfolioUrl());

        jobSeekerRepository.save(jobSeeker);
    }

    // ===============================
    // 🔹 Delete Social Links Section
    // ===============================

    @Transactional
    public void deleteSocialLinks() {
    	
    	Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        jobSeeker.setLinkedinUrl(null);
        jobSeeker.setGithubUrl(null);
        jobSeeker.setPortfolioUrl(null);

        jobSeekerRepository.save(jobSeeker);
    }
     
    
    
    @Transactional
    public ProfileCompletionDTO calculateProfileCompletion(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElse(null);

        if (jobSeeker == null) {
            return new ProfileCompletionDTO(0, List.of("Create Profile"));
        }

        int percentage = 0;
        List<String> missing = new ArrayList<>();

        // 🔹 Headline
        if (jobSeeker.getHeadline() != null && !jobSeeker.getHeadline().isBlank()) {
            percentage += 5;
        } else {
            missing.add("Add headline");
        }

        // 🔹 About
        if (jobSeeker.getAbout() != null && !jobSeeker.getAbout().isBlank()) {
            percentage += 5;
        } else {
            missing.add("Add about section");
        }

        // 🔹 Location
        if (jobSeeker.getCurrentLocation() != null && !jobSeeker.getCurrentLocation().isBlank()) {
            percentage += 5;
        } else {
            missing.add("Add location");
        }

        // 🔹 Skills
        if (jobSeeker.getSkills() != null && !jobSeeker.getSkills().isEmpty()) {
            percentage += 15;
        } else {
            missing.add("Add skills");
        }

        // 🔹 Education
        if (educationRepository.existsByUserId(userId)) {
            percentage += 15;
        } else {
            missing.add("Add education");
        }

        // 🔹 Experience
        if (jobSeeker.getTotalExperience() != null) {
            if (jobSeeker.getTotalExperience() == 0) {
                percentage += 15;
            } else if (experienceRepository.existsByUserId(userId)) {
                percentage += 15;
            } else {
                missing.add("Add experience");
            }
        } else {
            missing.add("Add experience");
        }

        // 🔹 Resume
        if (jobSeeker.getResumeUrl() != null && !jobSeeker.getResumeUrl().isBlank()) {
            percentage += 10;
        } else {
            missing.add("Upload resume");
        }

        // 🔹 Notice / Availability
        if (jobSeeker.getNoticeStatus() != null &&
            jobSeeker.getNoticePeriod() != null &&
            jobSeeker.getAvailableFrom() != null) {
            percentage += 10;
        } else {
            missing.add("Add availability details");
        }

        // 🔹 Expected CTC
        if (jobSeeker.getExpectedCTC() != null) {
            percentage += 10;
        } else {
            missing.add("Add expected salary");
        }

        // 🔹 Profile Image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            percentage += 10;
        } else {
            missing.add("Upload profile photo");
        }

        // 🔹 Internships
        if (!internshipRepository.existsByUserId(userId)) {
            missing.add("Add internships");
        } else {
            percentage += 5;
        }

        // 🔹 Projects
        if (!projectRepository.existsByUserId(userId)) {
            missing.add("Add projects");
        } else {
            percentage += 5;
        }

        // 🔹 Certifications
        if (!certificationRepository.existsByUserId(userId)) {
            missing.add("Add certifications");
        } else {
            percentage += 5;
        }

        return new ProfileCompletionDTO(percentage, missing);
    }


    
    @Transactional(readOnly = true)
    public AboutInfoDTO getMyAboutInfo() {

        Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        AboutInfoDTO dto = new AboutInfoDTO();
        dto.setHeadline(jobSeeker.getHeadline());
        dto.setAbout(jobSeeker.getAbout());

        return dto;
    }

    @Transactional(readOnly = true)
    public SocialLinksDTO getMySocialLinks() {

        Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        SocialLinksDTO dto = new SocialLinksDTO();
        dto.setLinkedinUrl(jobSeeker.getLinkedinUrl());
        dto.setGithubUrl(jobSeeker.getGithubUrl());
        dto.setPortfolioUrl(jobSeeker.getPortfolioUrl());

        return dto;
    }

    @Transactional(readOnly = true)
    public AboutInfoDTO getAboutInfoByUserId(Long userId) {

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        AboutInfoDTO dto = new AboutInfoDTO();
        dto.setHeadline(jobSeeker.getHeadline());
        dto.setAbout(jobSeeker.getAbout());

        return dto;
    }

    
    @Transactional(readOnly = true)
    public SocialLinksDTO getSocialLinksByUserId(Long userId) {

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        SocialLinksDTO dto = new SocialLinksDTO();
        dto.setLinkedinUrl(jobSeeker.getLinkedinUrl());
        dto.setGithubUrl(jobSeeker.getGithubUrl());
        dto.setPortfolioUrl(jobSeeker.getPortfolioUrl());

        return dto;
    }

    @Transactional(readOnly = true)
    public JobSeekerProfileSummaryResponse getMyProfileSummary() {

        Long userId = SecurityUtils.getUserId();

        JobSeeker jobSeeker = jobSeekerRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        JobSeekerProfileSummaryResponse response = new JobSeekerProfileSummaryResponse();
        response.setHeadline(jobSeeker.getHeadline());
        response.setAbout(jobSeeker.getAbout());
        response.setLinkedinUrl(jobSeeker.getLinkedinUrl());
        response.setGithubUrl(jobSeeker.getGithubUrl());
        response.setPortfolioUrl(jobSeeker.getPortfolioUrl());
        response.setProfileCompletion(jobSeeker.getProfileCompletion());

        return response;
    }



    
    

    @Transactional(readOnly = true)
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

        Pageable pageable = buildPageable(request);

        Page<JobSeeker> page = jobSeekerRepository.findAll(specification, pageable);

        saveJobSeekerSearchHistory(request, page.getTotalElements());

        List<Long> seekerIds = page.getContent().stream()
                .map(JobSeeker::getId)
                .toList();

        if (seekerIds.isEmpty()) {
            return PaginationUtil.buildPagedResponse(page, Collections.emptyList());
        }

        List<JobSeeker> hydratedSeekers = jobSeekerRepository.findByIdIn(seekerIds);

        Map<Long, JobSeeker> seekerMap = hydratedSeekers.stream()
                .collect(Collectors.toMap(
                        JobSeeker::getId,
                        js -> js,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<Long> userIds = hydratedSeekers.stream()
                .map(JobSeeker::getUser)
                .filter(Objects::nonNull)
                .map(user -> user.getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Resume> resumeByUserId = userIds.isEmpty()
                ? Collections.emptyMap()
                : resumeRepository.findByUserIdInAndIsDefaultTrueAndDeletedFalse(userIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Resume::getUserId,
                                resume -> resume,
                                (r1, r2) -> {
                                    // keep the latest updated resume if duplicates somehow exist
                                    if (r1.getUpdatedAt() == null) return r2;
                                    if (r2.getUpdatedAt() == null) return r1;
                                    return r1.getUpdatedAt().isAfter(r2.getUpdatedAt()) ? r1 : r2;
                                }
                        ));
        
     // 🔥 STEP 1: Collect resumeIds
        List<Long> resumeIds = resumeByUserId.values().stream()
                .map(Resume::getId)
                .filter(Objects::nonNull)
                .toList();

        // 🔥 STEP 2: Fetch counts in ONE query
        Map<Long, Long> resumeViewCountMap = resumeIds.isEmpty()
                ? Collections.emptyMap()
                : resumeViewHistoryRepository.countViewsByResumeIds(resumeIds)
                .stream()
                .collect(Collectors.toMap(
                        ResumeViewCountProjection::getResumeId,
                        ResumeViewCountProjection::getViewCount
                ));

        List<JobSeekerSearchResponse> content = seekerIds.stream()
                .map(seekerMap::get)
                .filter(Objects::nonNull)
                .map(js -> {
                    Long userId = js.getUser() != null ? js.getUser().getId() : null;
                    Resume resume = userId != null ? resumeByUserId.get(userId) : null;

                    Long viewCount = 0L;
                    if (resume != null) {
                        viewCount = resumeViewCountMap.getOrDefault(resume.getId(), 0L);
                    }

                    return toSearchResponse(js, resume, viewCount);
                })
                .toList();

        return PaginationUtil.buildPagedResponse(page, content);
    }

    private Pageable buildPageable(JobSeekerSearchRequest request) {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;

        boolean hasSkills = request.getSkills() != null && !request.getSkills().isEmpty();
        boolean hasKeyword = request.getKeyword() != null && !request.getKeyword().isBlank();

        if (hasSkills || hasKeyword) {
            return PageRequest.of(page, size);
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (request.getSortDirection() != null && !request.getSortDirection().isBlank()) {
            try {
                direction = Sort.Direction.fromString(request.getSortDirection());
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid sortDirection '{}', defaulting to DESC", request.getSortDirection());
            }
        }

        String sortBy = (request.getSortBy() != null && !request.getSortBy().isBlank())
                ? request.getSortBy()
                : "totalExperience";

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private void saveJobSeekerSearchHistory(
            JobSeekerSearchRequest request,
            long totalResults
    ) {
        try {
            Long userId = SecurityUtils.getUserId();
            Role role = SecurityUtils.getRole();

            if (userId == null || role == null) {
                return;
            }

            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("skills", request.getSkills());
            filters.put("currentLocation", request.getCurrentLocation());
            filters.put("preferredLocation", request.getPreferredLocation());
            filters.put("minExperience", request.getMinExperience());
            filters.put("maxExperience", request.getMaxExperience());
            filters.put("minExpectedCTC", request.getMinExpectedCTC());
            filters.put("maxExpectedCTC", request.getMaxExpectedCTC());
            filters.put("noticeStatus",
                    request.getNoticeStatus() != null ? request.getNoticeStatus().name() : null);
            filters.put("maxNoticePeriod", request.getMaxNoticePeriod());
            filters.put("immediateJoiner", request.getImmediateJoiner());
            filters.put("availableBefore",
                    request.getAvailableBefore() != null ? request.getAvailableBefore().toString() : null);
            filters.put("sortBy", request.getSortBy());
            filters.put("sortDirection", request.getSortDirection());

            searchHistoryService.saveSearchHistory(
                    SaveSearchHistoryRequest.builder()
                            .userId(userId)
                            .role(role)
                            .module(SearchModule.CANDIDATES)
                            .searchType(SearchType.CANDIDATE)
                            .keyword(request.getKeyword())
                            .filters(filters)
                            .resultCount(safeLongToInt(totalResults))
                            .sourcePage("/jobseekers")
                            .build()
            );

        } catch (Exception e) {
            log.warn("Failed to save job seeker search history", e);
        }
    }

    private int safeLongToInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
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
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
   
    private JobSeekerSearchResponse toSearchResponse(JobSeeker jobSeeker, Resume resume, Long viewCount) {

        User user = jobSeeker.getUser();

        List<String> skillNames = jobSeeker.getSkills() != null
                ? jobSeeker.getSkills().stream()
                    .map(Skill::getName)
                    .toList()
                : List.of();

        return JobSeekerSearchResponse.builder()
                .id(jobSeeker.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(user != null ? user.getName() : null)
                .email(user != null ? user.getEmail() : null)
                .currentCompany(jobSeeker.getCurrentCompany())
                .totalExperience(jobSeeker.getTotalExperience())
                .expectedCTC(jobSeeker.getExpectedCTC())
                .currentLocation(jobSeeker.getCurrentLocation())
                .immediateJoiner(jobSeeker.getImmediateJoiner())
                .noticePeriod(jobSeeker.getNoticePeriod())
                .skills(skillNames)

                // resume fields
                .resumeId(resume != null ? resume.getId() : null)
                .resumeUploadedAt(resume != null ? resume.getUploadedAt() : null)
                .resumeUpdatedAt(resume != null ? resume.getUpdatedAt() : null)
                .resumeUrl(resume != null? resume.getSecureUrl() : null)

                // profile fields
                .createdAt(jobSeeker.getCreatedAt())
                .updatedAt(jobSeeker.getUpdatedAt())
                .resumeViewCount(viewCount)
                .build();
    }


}
