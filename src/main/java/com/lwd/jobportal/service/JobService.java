package com.lwd.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.dto.companydto.CompanySummaryDTO;
import com.lwd.jobportal.dto.jobdto.*;
import com.lwd.jobportal.entity.*;
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
        Job job = getJobByIdInternal(jobId);

        validateOwnership(user, job);

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setIndustry(request.getIndustry());
        job.setMinExperience(request.getMinExperience());
        job.setMaxExperience(request.getMaxExperience());
        job.setJobType(request.getJobType());            

        return mapToResponse(jobRepository.save(job));
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
    // GET JOBS
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

        Page<JobResponse> jobPage;

        // ================= ADMIN =================
        if (role == Role.ADMIN) {

            jobPage = jobRepository
                    .findAll(pageable)
                    .map(this::mapToResponse);
        }
        // ================= RECRUITER_ADMIN =================
        else if (role == Role.RECRUITER_ADMIN) {

            Company company = companyRepository
                    .findByCreatedById(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Company not found for recruiter admin"));

            jobPage = jobRepository
                    .findByCompanyId(company.getId(), pageable)
                    .map(this::mapToResponse);
        }
        // ================= RECRUITER =================
        else if (role == Role.RECRUITER) {

            jobPage = jobRepository
                    .findByCreatedById(userId, pageable)
                    .map(this::mapToResponse);
        }
        // ================= INVALID =================
        else {
            throw new AccessDeniedException("Unauthorized role");
        }

        return toPagedResponse(jobPage);
    }

    

    // ==================================================
    // READ APIs
    // ==================================================
    public JobResponse getJobById(Long jobId) {
        return mapToResponse(getJobByIdInternal(jobId));
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
    

    public PagedJobResponse getAllJobs(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Job> jobPage =
                jobRepository.findAll(JobSpecification.publicJobs(), pageable);

        return toPagedResponse(jobPage.map(this::mapToResponse));
    }



    
    public List<String> getTopIndustries(int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        return jobRepository.findTopIndustries(pageable)
                .stream()
                .map(IndustryCount::getIndustry)
                .toList();
    }

    
    
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



    
    public List<JobResponse> getTrendingJobs() {

        Pageable pageable = PageRequest.of(0, 10);

        return jobRepository.findTrendingJobs(pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    






    // ==================================================
    // PRIVATE HELPERS
    // ==================================================
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Company getCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    private Job getJobByIdInternal(Long jobId) {
        return jobRepository.findByIdAndDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }


    private void validateOwnership(User user, Job job) {
        if (user.getRole() != Role.ADMIN &&
                !job.getCompany().getCreatedById().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not allowed to modify jobs from another company"
            );
        }
    }

    private Job buildJob(CreateJobRequest request, User creator, Company company) {
        return Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .salary(request.getSalary())
                .industry(request.getIndustry())
                .minExperience(request.getMinExperience())   // new
                .maxExperience(request.getMaxExperience())   // new
                .jobType(request.getJobType())              // new
                .company(company)
                .createdBy(creator)
                .build();
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
                .salary(job.getSalary())
                .status(job.getStatus().name())
                .industry(job.getIndustry())
                .createdBy(job.getCreatedBy().getEmail())
                .createdAt(job.getCreatedAt())
                .minExperience(job.getMinExperience())
                .maxExperience(job.getMaxExperience())
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)

                // ===== LWD FIELDS =====
                .noticePreference(
                        job.getNoticePreference() != null
                                ? job.getNoticePreference().name()
                                : null
                )
                .maxNoticePeriod(job.getMaxNoticePeriod())
                .lwdPreferred(job.getLwdPreferred())

                .company(
                        CompanySummaryDTO.builder()
                                .id(company.getId())
                                .companyName(company.getCompanyName())
                                .logo(company.getLogoUrl())
                                .build()
                )
                .build();
    }
    
}
