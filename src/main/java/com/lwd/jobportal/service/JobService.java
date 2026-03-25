package com.lwd.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.comman.PaginationUtil;
import com.lwd.jobportal.dto.companydto.CompanySummaryDTO;
import com.lwd.jobportal.dto.jobdto.*;
import com.lwd.jobportal.entity.*;
import com.lwd.jobportal.enums.ApplicationSource;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.*;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.specification.IndustryCount;
import com.lwd.jobportal.specification.JobSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final JobApplicationRepository jobApplicationRepository;

    // ==================================================
    // ADMIN CREATE JOB
    // ==================================================
    @PreAuthorize("hasRole('ADMIN')")
    public JobResponse createJobAsAdmin(CreateJobRequest request, Long adminId, Long companyId) {
        User admin = getUserById(adminId);
        Company company = getCompanyById(companyId);

        Job job = buildJob(request, admin, company);
        job.setStatus(JobStatus.OPEN);

        return mapToResponse(jobRepository.save(job));
    }
    
    
    // ==================================================
    // CREATE JOB BY RECRUITER
    // ==================================================
    @Transactional
    public JobResponse createJobAsRecruiter(CreateJobRequest request) {

        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🔒 Only ACTIVE users
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("User is not approved");
        }

        Company company;

        // ================= RECRUITER_ADMIN =================
        if (role == Role.RECRUITER_ADMIN) {

            company = companyRepository.findByCreatedById(userId)
                    .orElseThrow(() ->
                            new IllegalStateException("Recruiter Admin does not own any company"));

        }
        // ================= RECRUITER =================
        else if (role == Role.RECRUITER) {

            if (user.getCompany() == null) {
                throw new AccessDeniedException("Recruiter is not assigned to any company");
            }

            company = user.getCompany();
        }
        // ================= INVALID =================
        else {
            throw new AccessDeniedException("Invalid role for job creation");
        }
        
        if (request.getApplicationSource() == ApplicationSource.EXTERNAL &&
        		(request.getExternalApplicationUrl() == null || request.getExternalApplicationUrl().isBlank())) {

        	    throw new IllegalArgumentException("External application URL is required for EXTERNAL jobs");
        	}

        Job job = buildJob(request, user, company);
        job.setStatus(JobStatus.OPEN);

        return mapToResponse(jobRepository.save(job));
    }

	 // ==================================================
	 // UPDATE JOB
	 // ==================================================
	 @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
	 public JobResponse updateJob(Long jobId, CreateJobRequest request, Long userId) {
	
	     User user = getUserById(userId);
	
	     Job job = jobRepository.findById(jobId)
	             .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
	
	     validateOwnership(user, job);
	
	     // ===== BASIC INFO =====
	     job.setTitle(request.getTitle());
	     job.setDescription(request.getDescription());
	     job.setLocation(request.getLocation());
	     job.setIndustry(request.getIndustry());
	
	     // ===== SALARY RANGE =====
	     job.setMinSalary(request.getMinSalary());
	     job.setMaxSalary(request.getMaxSalary());
	
	     // ===== EXPERIENCE =====
	     job.setMinExperience(request.getMinExperience());
	     job.setMaxExperience(request.getMaxExperience());
	
	     // ===== JOB DETAILS =====
	     job.setJobType(request.getJobType());
	     job.setRoleCategory(request.getRoleCategory());
	     job.setDepartment(request.getDepartment());
	     job.setWorkplaceType(request.getWorkplaceType());
	
	     // ===== JOB CONTENT =====
	     job.setResponsibilities(request.getResponsibilities());
	     job.setRequirements(request.getRequirements());
	     job.setBenefits(request.getBenefits());
	
	     // ===== LWD SETTINGS =====
	     job.setNoticePreference(request.getNoticePreference());
	     job.setMaxNoticePeriod(request.getMaxNoticePeriod());
	
	     if (request.getLwdPreferred() != null) {
	         job.setLwdPreferred(request.getLwdPreferred());
	     }
	
	     // ===== APPLICATION SOURCE =====
	     if (request.getApplicationSource() != null) {
	         job.setApplicationSource(request.getApplicationSource());
	         job.setExternalApplicationUrl(request.getExternalApplicationUrl());
	     }
	
	     Job updatedJob = jobRepository.save(job);
	
	     return mapToResponse(updatedJob);
	 }


    // ==================================================
    // DELETE JOB
    // ==================================================
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @Transactional
    public void deleteJob(Long jobId, Long userId) {

        User user = getUserById(userId);
        Job job = getJobByIdInternal(jobId);

        validateOwnership(user, job);

        // 🔥 Soft delete
        job.setDeleted(true);
        job.setDeletedAt(LocalDateTime.now());
        job.setStatus(JobStatus.CLOSED); // Optional but recommended

        jobRepository.save(job);
    }


    // ==================================================
    // CHANGE JOB STATUS
    // ==================================================
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    public JobResponse changeJobStatus(Long jobId, JobStatus status, Long userId) {

        User user = getUserById(userId);
        Job job = getJobByIdInternal(jobId);

        validateOwnership(user, job);

        job.setStatus(status);
        return mapToResponse(jobRepository.save(job));
    }
    
    
    // ==================================================
    // GET JOBS by role
    // ==================================================
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    public PagedJobResponse getMyJobs(int page) {

        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        Pageable pageable = PageRequest.of(
                page,
                12,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Job> jobPageRaw;

        // ================= ADMIN =================
        if (role == Role.ADMIN) {

            jobPageRaw = jobRepository.findAll(pageable);
        }
        // ================= RECRUITER_ADMIN =================
        else if (role == Role.RECRUITER_ADMIN) {

            Company company = companyRepository
                    .findByCreatedById(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Company not found for recruiter admin"));

            jobPageRaw = jobRepository
                    .findByCompanyId(company.getId(), pageable);
        }
        // ================= RECRUITER =================
        else if (role == Role.RECRUITER) {

            jobPageRaw = jobRepository
                    .findByCreatedById(userId, pageable);
        }
        // ================= INVALID =================
        else {
            throw new AccessDeniedException("Unauthorized role");
        }

        // ================= FETCH APPLICATION COUNTS (NO N+1) =================

        List<Long> jobIds = jobPageRaw.getContent()
                .stream()
                .map(Job::getId)
                .toList();

        Map<Long, Long> countMap = new HashMap<>();

        if (!jobIds.isEmpty()) {

            List<Object[]> counts =
                    jobApplicationRepository.countApplicationsForJobs(jobIds);

            for (Object[] row : counts) {
                Long jobId = (Long) row[0];
                Long count = (Long) row[1];
                countMap.put(jobId, count);
            }
        }

        // ================= MAP TO DTO =================

        Page<JobResponse> jobPage = jobPageRaw.map(
                job -> mapToResponse(job, countMap)
        );

        return toPagedResponse(jobPage);
    }
    
    
    public PagedJobResponse searchJobsByRole(String keyword, int page) {

        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        Pageable pageable = PageRequest.of(page, 12, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Job> jobPageRaw;

        if (role == Role.ADMIN) {
            jobPageRaw = jobRepository.searchAllJobs(keyword, pageable);
        } else if (role == Role.RECRUITER_ADMIN) {
            Company company = companyRepository.findByCreatedById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found for recruiter admin"));

            jobPageRaw = jobRepository.searchJobsByCompany(company.getId(), keyword, pageable);
        } else if (role == Role.RECRUITER) {
            jobPageRaw = jobRepository.searchJobsByCreator(userId, keyword, pageable);
        } else {
            throw new AccessDeniedException("Unauthorized role");
        }

        // ================= FETCH APPLICATION COUNTS =================
        List<Long> jobIds = jobPageRaw.getContent().stream()
                .map(Job::getId)
                .toList();

        Map<Long, Long> countMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            List<Object[]> counts = jobApplicationRepository.countApplicationsForJobs(jobIds);
            for (Object[] row : counts) {
                countMap.put((Long) row[0], (Long) row[1]);
            }
        }

        // ================= MAP TO DTO =================
        Page<JobResponse> jobPage = jobPageRaw.map(job -> mapToResponse(job, countMap));

        return toPagedResponse(jobPage);
    }
    
    @Transactional(readOnly = true)
    public PagedJobResponse searchJobsByRole(JobSearchRequest request, int page) {

        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        Pageable pageable = PageRequest.of(
                page,
                12,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Long companyId = null;

        if (role == Role.RECRUITER_ADMIN) {
            Company company = companyRepository.findByCreatedById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            companyId = company.getId();
        }

        Page<Job> jobPage = jobRepository.findAll(
                JobSpecification.searchJobsByRole(userId, companyId, role, request),
                pageable
        );

        return toPagedResponse(jobPage.map(job -> mapToResponse(job, Map.of())));
    }

    

    // ==================================================
    // GET JOB BY ID
    // ==================================================
    public JobResponse getJobById(Long jobId) {

        Job job = getJobByIdInternal(jobId);

        // get application count
        Long totalApplications =
                jobApplicationRepository.countByJobId(jobId);

        Map<Long, Long> countMap = Map.of(jobId, totalApplications);

        return mapToResponse(job, countMap);
    }

    
    
    public PagedResponse<JobResponse> getJobsByRecruiter(
            Long recruiterId,
            int page,
            int size
    ) {

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("User is not a recruiter");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Job> jobPage = jobRepository
                .findByCreatedById(recruiterId, pageable);

        List<JobResponse> content = jobPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedResponse<>(
                content,
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements(),
                jobPage.getTotalPages(),
                jobPage.isLast()
        );
    }


    
    public PagedJobResponse getJobsByCompany(Long companyId, int page) {

        Pageable pageable = PageRequest.of(
                page,
                12,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<JobResponse> jobPage = jobRepository
                .findByCompanyId(companyId, pageable)
                .map(this::mapToResponse);

        return toPagedResponse(jobPage);
    }
    
    
    public JobAnalyticsResponse getJobAnalytics(Long jobId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Status-wise count
        List<Object[]> results =
                jobApplicationRepository.countApplicationsByStatus(jobId);

        Map<String, Long> statusCounts = new HashMap<>();
        Long total = 0L;

        for (Object[] row : results) {
            String status = row[0].toString();
            Long count = (Long) row[1];

            statusCounts.put(status, count);
            total += count;
        }

        return JobAnalyticsResponse.builder()
                .job(mapToResponse(job))
                .totalApplications(total)
                .statusCounts(statusCounts)
                .build();
    }

    

    // ==================================================
    // GET ALL JOBS
    // ==================================================
    public PagedJobResponse getAllJobs(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Job> jobPage =
                jobRepository.findAll(JobSpecification.publicJobs(), pageable);

        // =============================
        // Extract job IDs
        // =============================
        List<Long> jobIds = jobPage.getContent()
                .stream()
                .map(Job::getId)
                .toList();

        // =============================
        // Fetch application counts
        // =============================
        Map<Long, Long> countMap = new HashMap<>();

        if (!jobIds.isEmpty()) {

            List<Object[]> counts =
                    jobApplicationRepository.countApplicationsForJobs(jobIds);

            for (Object[] row : counts) {

                Long jobId = (Long) row[0];
                Long count = ((Number) row[1]).longValue();

                countMap.put(jobId, count);
            }
        }

        // =============================
        // Map response
        // =============================
        Page<JobResponse> responsePage =
                jobPage.map(job -> mapToResponse(job, countMap));

        return toPagedResponse(responsePage);
    }



    // ==================================================
    // GET TOP INDUSTRIES
    // ==================================================
    public List<String> getTopIndustries(int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        return jobRepository.findTopIndustries(pageable)
                .stream()
                .map(IndustryCount::getIndustry)
                .toList();
    }

    
    
    // ==================================================
    // GET JOB BY iNDUSTRIES
    // ==================================================
    public PagedJobResponse getJobsByIndustry(String industry, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobPage = jobRepository.findByIndustryIgnoreCaseAndStatus(industry, JobStatus.OPEN, pageable);

        List<JobResponse> jobResponses = jobPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedJobResponse(
                jobResponses,
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements(),
                jobPage.getTotalPages(),
                jobPage.isLast()
                );
    }
     
    
	 // ==================================================
	 // SEARCH PUBLIC JOBS
	 // ==================================================
	 public PagedJobResponse searchPublicJobs(
	         String keyword,
	         String location,
	         String industry,
	         String companyName,
	         Integer minExp,
	         Integer maxExp,
	         JobType jobType,
	
	         // ===== LWD FILTERS =====
	         NoticeStatus noticePreference,
	         Integer maxNoticePeriod,
	         Boolean lwdPreferred,
	
	         int page,
	         int size
	 ) {
	
	     Pageable pageable = PageRequest.of(
	             page,
	             size,
	             Sort.by(Sort.Direction.DESC, "createdAt")
	     );
	
	     Specification<Job> spec = JobSpecification.searchJobs(
	             keyword,
	             location,
	             industry,
	             companyName,
	             minExp,
	             maxExp,
	             jobType,
	             noticePreference,
	             maxNoticePeriod,
	             lwdPreferred,
	             null,      // status not allowed for public
	             true       // isPublicRequest = true
	     );
	
	     Page<Job> jobPage = jobRepository.findAll(spec, pageable);
	
	     return toPagedResponse(jobPage.map(this::mapToResponse));
	 }


	 
	// ==================================================
	// SUGGESTED JOBS
	// ==================================================

    public PagedJobResponse getSuggestedJobs(Long userId, int page, int size) {
        // 1️⃣ Get last applied job
        List<Job> appliedJobs = jobRepository.findJobsByUserIdOrderByAppliedAtDesc(userId);
        if (appliedJobs.isEmpty()) {
            throw new ResourceNotFoundException("No job history found");
        }
        Job lastAppliedJob = appliedJobs.get(0);

        // 2️⃣ Fetch suggested jobs with single query
        List<Job> suggestedJobs = jobRepository.findSuggestedJobs(
                userId,
                lastAppliedJob.getIndustry(),
                lastAppliedJob.getLocation(),
                PageRequest.of(page, size)
        );

        // 3️⃣ Convert to JobResponse
        List<JobResponse> jobResponses = suggestedJobs.stream()
                .map(this::mapToResponse)
                .toList();

        // 4️⃣ Wrap in Page object (for convenience)
        Page<JobResponse> jobPage = new PageImpl<>(
                jobResponses,
                PageRequest.of(page, size),
                suggestedJobs.size() // For real pagination, consider separate count query
        );

        // 5️⃣ Convert to PagedJobResponse DTO
        return toPagedResponse(jobPage);
    }


    // ==================================================
    // SIMILAR JOBS
    // ==================================================
    
    @Transactional(readOnly = true)
    public List<JobResponse> getSimilarJobs(Long jobId) {

        Job job = getJobByIdInternal(jobId);

        Specification<Job> spec = JobSpecification.similarJobs(
                job.getIndustry(),
                job.getJobType(),
                job.getId()
        );

        Pageable pageable = PageRequest.of(
                0,
                6,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return jobRepository.findAll(spec, pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    
    // ==================================================
    // JOB SEARCH SUGGESTIONS
    // ==================================================
    public List<String> getSearchSuggestions(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        Pageable limit = PageRequest.of(0, 3); // max 3 from each category

        List<String> suggestions = new ArrayList<>(10);

        // 1️⃣ Title
        suggestions.addAll(
                jobRepository.findTitleSuggestions(lowerKeyword, limit)
        );

        // 2️⃣ Location
        suggestions.addAll(
                jobRepository.findLocationSuggestions(lowerKeyword, limit)
        );

        // 3️⃣ Company
        suggestions.addAll(
                jobRepository.findCompanySuggestions(lowerKeyword, limit)
        );

        // 4️⃣ Industry
        suggestions.addAll(
                jobRepository.findIndustrySuggestions(lowerKeyword, limit)
        );

        return suggestions.stream()
                .distinct()
                .limit(10)
                .toList();
    }


    // ==================================================
    // TRENDING JOBS
    // ==================================================
    
    public List<JobResponse> getTrendingJobs() {

        Pageable pageable = PageRequest.of(0, 10);

        return jobRepository.findTrendingJobs(pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    
    
    
    public PagedResponse<JobResponse> getRecommendedJobs(
	        int page,
	        int size
	) {
	
	    Long userId = SecurityUtils.getUserId();
	
	    JobSeeker seeker = jobSeekerRepository
	            .findByUserId(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
	
	    Specification<Job> spec =
	            JobSpecification.recommendedJobs(seeker);
	
	    Pageable pageable = PageRequest.of(page, size);
	
	    Page<Job> jobPage = jobRepository.findAll(spec, pageable);
	
	    List<JobResponse> content = jobPage.stream()
	            .map(job -> mapToRecommendedJobResponse(job, seeker))
	            .sorted(Comparator.comparing(JobResponse::getMatchScore).reversed())
	            .toList();
	
	    return PaginationUtil.buildPagedResponse(jobPage, content);
	}


    



    // ==================================================
    // PRIVATE HELPERS
    // ==================================================
    
    //================= GET USER BY ID =================
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    
    //================= ADMIN =================
    private Company getCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    //================= GET JOB ID =================
    private Job getJobByIdInternal(Long jobId) {
        return jobRepository.findByIdAndDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

 // ================= ROLE BASED VALIDATION =================
    private void validateOwnership(User user, Job job) {

        // ADMIN can update anything
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        // RECRUITER_ADMIN -> jobs of their company
        if (user.getRole() == Role.RECRUITER_ADMIN) {
            if (!job.getCompany().getCreatedById().equals(user.getId())) {
                throw new IllegalArgumentException(
                        "You can only modify jobs of your company"
                );
            }
            return;
        }

        // RECRUITER -> only jobs they created
        if (user.getRole() == Role.RECRUITER) {
            if (!job.getCreatedBy().getId().equals(user.getId())) {
                throw new IllegalArgumentException(
                        "You can only modify jobs you created"
                );
            }
            return;
        }

        throw new IllegalArgumentException("You are not allowed to update jobs");
    }


    private Job buildJob(CreateJobRequest request, User createdBy, Company company) {

        Job.JobBuilder builder = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .industry(request.getIndustry())

                // ================= SALARY =================
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())

                // ================= EXPERIENCE =================
                .minExperience(request.getMinExperience())
                .maxExperience(request.getMaxExperience())

                // ================= JOB DETAILS =================
                .jobType(request.getJobType())
                .roleCategory(request.getRoleCategory())
                .department(request.getDepartment())
                .workplaceType(request.getWorkplaceType())

                // ================= CANDIDATE PREFERENCES =================
                .education(request.getEducation())
                .skills(request.getSkills())
                .genderPreference(request.getGenderPreference())
                .ageLimit(request.getAgeLimit())

                // ================= JOB CONTENT =================
                .responsibilities(request.getResponsibilities())
                .requirements(request.getRequirements())
                .benefits(request.getBenefits())

                // ================= LWD FEATURES =================
                .noticePreference(request.getNoticePreference())
                .maxNoticePeriod(request.getMaxNoticePeriod())
                .lwdPreferred(request.getLwdPreferred() != null ? request.getLwdPreferred() : false)

                // ================= RELATIONS =================
                .company(company)
                .createdBy(createdBy);

        // ================= APPLICATION SOURCE =================

        ApplicationSource source = request.getApplicationSource() != null
                ? request.getApplicationSource()
                : ApplicationSource.PORTAL;

        builder.applicationSource(source);

        if (source == ApplicationSource.EXTERNAL) {

            if (request.getExternalApplicationUrl() == null ||
                request.getExternalApplicationUrl().isBlank()) {

                throw new IllegalArgumentException(
                        "External URL must be provided when application source is EXTERNAL"
                );
            }

            builder.externalApplicationUrl(request.getExternalApplicationUrl());
        }

        return builder.build();
    }




    private PagedJobResponse toPagedResponse(Page<JobResponse> page) {
        return new PagedJobResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    

    private JobResponse mapToResponse(Job job) {

        Company company = job.getCompany();
        

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .industry(job.getIndustry())

                // ===== SALARY RANGE =====
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())

                // ===== JOB STATUS =====
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .deleted(job.getDeleted())

                // ===== EXPERIENCE =====
                .minExperience(job.getMinExperience())
                .maxExperience(job.getMaxExperience())

                // ===== JOB DETAILS =====
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)
                .roleCategory(job.getRoleCategory())
                .department(job.getDepartment())
                .workplaceType(job.getWorkplaceType())

                // ===== JOB CONTENT =====
                .responsibilities(job.getResponsibilities())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())

                // ===== CREATED INFO =====
                .createdBy(
                        job.getCreatedBy() != null
                                ? job.getCreatedBy().getEmail()
                                : null
                )
                .createdAt(job.getCreatedAt())

                // ===== LWD FIELDS =====
                .noticePreference(
                        job.getNoticePreference() != null
                                ? job.getNoticePreference().name()
                                : null
                )
                .maxNoticePeriod(job.getMaxNoticePeriod())
                .lwdPreferred(job.getLwdPreferred())

                // ===== APPLICATION SOURCE =====
                .applicationSource(job.getApplicationSource())
                .externalApplicationUrl(job.getExternalApplicationUrl())

                // ===== COMPANY =====
                .company(
                        company != null
                                ? CompanySummaryDTO.builder()
                                        .id(company.getId())
                                        .companyName(company.getCompanyName())
                                        .logo(company.getLogoUrl())
                                        .build()
                                : null
                )

                .build();
    }

    
    
    
    private JobResponse mapToResponse(Job job, Map<Long, Long> countMap) {

        Company company = job.getCompany();

        Long totalApplications = (countMap != null)
                ? countMap.getOrDefault(job.getId(), 0L)
                : 0L;

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .industry(job.getIndustry())

                // ===== SALARY RANGE =====
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())

                // ===== STATUS =====
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .deleted(job.getDeleted())

                // ===== EXPERIENCE =====
                .minExperience(job.getMinExperience())
                .maxExperience(job.getMaxExperience())

                // ===== JOB DETAILS =====
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)
                .roleCategory(job.getRoleCategory())
                .department(job.getDepartment())
                .workplaceType(job.getWorkplaceType())

                // ===== JOB CONTENT =====
                .responsibilities(job.getResponsibilities())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())

                // ===== CREATED INFO =====
                .createdBy(
                        job.getCreatedBy() != null
                                ? job.getCreatedBy().getEmail()
                                : null
                )
                .createdAt(job.getCreatedAt())

                // ===== APPLICATION COUNT =====
                .totalApplications(totalApplications)

                // ===== LWD FIELDS =====
                .noticePreference(
                        job.getNoticePreference() != null
                                ? job.getNoticePreference().name()
                                : null
                )
                .maxNoticePeriod(job.getMaxNoticePeriod())
                .lwdPreferred(job.getLwdPreferred())

                // ===== APPLICATION SOURCE =====
                .applicationSource(job.getApplicationSource())
                .externalApplicationUrl(job.getExternalApplicationUrl())

                // ===== COMPANY =====
                .company(
                        company != null
                                ? CompanySummaryDTO.builder()
                                        .id(company.getId())
                                        .companyName(company.getCompanyName())
                                        .logo(company.getLogoUrl())
                                        .build()
                                : null
                )

                .build();
    }

    
    
    
    private int calculateMatchScore(Job job, JobSeeker seeker) {

	    int score = 0;
	
	    // =========================
	    // 1️⃣ EXPERIENCE (30%)
	    // =========================
	    if (seeker.getTotalExperience() != null &&
	            job.getMinExperience() != null &&
	            job.getMaxExperience() != null) {
	
	        int exp = seeker.getTotalExperience();
	
	        if (exp >= job.getMinExperience() &&
	                exp <= job.getMaxExperience()) {
	            score += 30;
	        }
	    }
	
	    // =========================
	    // 2️⃣ LOCATION (20%)
	    // =========================
	    if (seeker.getPreferredLocation() != null &&
	            job.getLocation() != null &&
	            job.getLocation().toLowerCase()
	                    .contains(seeker.getPreferredLocation().toLowerCase())) {
	        score += 20;
	    }
	
	    // =========================
	    // 3️⃣ INDUSTRY (15%)
	    // =========================
	    if (seeker.getCurrentCompany() != null &&
	            job.getIndustry() != null &&
	            job.getIndustry().toLowerCase()
	                    .contains(seeker.getCurrentCompany().toLowerCase())) {
	        score += 15;
	    }
	
	    // =========================
	    // 4️⃣ JOB TYPE (10%)
	    // =========================
	    if (job.getJobType() != null) {
	        score += 10; // you can refine later
	    }
	
	    // =========================
	    // 5️⃣ NOTICE MATCH (10%)
	    // =========================
	    if (seeker.getNoticePeriod() != null &&
	            job.getMaxNoticePeriod() != null &&
	            seeker.getNoticePeriod() <= job.getMaxNoticePeriod()) {
	        score += 10;
	    }
	
	 // =========================
	 // 6️⃣ SALARY MATCH (5%)
	 // =========================
	 if (seeker.getExpectedCTC() != null &&
	         job.getMinSalary() != null &&
	         job.getMaxSalary() != null &&
	         seeker.getExpectedCTC() >= job.getMinSalary() &&
	         seeker.getExpectedCTC() <= job.getMaxSalary()) {

	     score += 5;
	 }

	    // =========================
	    // 7️⃣ PROFILE BOOST (10%)
	    // =========================
	    if (seeker.getProfileCompletion() != null &&
	            seeker.getProfileCompletion() >= 75) {
	        score += 10;
	    }
	
	    return score;
    }


    
    private JobResponse mapToRecommendedJobResponse(
            Job job,
            JobSeeker seeker
    ) {

        JobResponse response = mapToResponse(job); // reuse old mapping

        int score = calculateMatchScore(job, seeker);

        response.setMatchScore(score);

        return response;
    }

    
}
