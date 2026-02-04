package com.lwd.jobportal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.companydto.CompanySummaryDTO;
import com.lwd.jobportal.entity.*;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.jobdto.*;
import com.lwd.jobportal.repository.*;
import com.lwd.jobportal.security.SecurityUtils;
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
    public void deleteJob(Long jobId, Long userId) {

        User user = getUserById(userId);
        Job job = getJobByIdInternal(jobId);

        validateOwnership(user, job);

        jobRepository.delete(job);
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
    // READ APIs
    // ==================================================
    public JobResponse getJobById(Long jobId) {
        return mapToResponse(getJobByIdInternal(jobId));
    }

    
    public PagedJobResponse getJobsByCompany(Long companyId, int page) {

        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<JobResponse> jobPage = jobRepository
                .findByCompanyId(companyId, pageable)
                .map(this::mapToResponse);

        return toPagedResponse(jobPage);
    }
    

    public PagedJobResponse getAllJobs(int page) {

        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<JobResponse> jobPage = jobRepository
                .findAll(pageable)
                .map(this::mapToResponse);

        return new PagedJobResponse(
                jobPage.getContent(),
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements(),
                jobPage.getTotalPages(),
                jobPage.isLast()
        );
    }


    // ==================================================
    // LATEST JOBS (Cursor + Pagination)
    // ==================================================
    public List<JobResponse> getLatestJobs(LocalDateTime lastSeen, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<Job> jobs;

        if (lastSeen != null) {
            jobs = jobRepository.findByStatusAndCreatedAtLessThanOrderByCreatedAtDesc(
                    JobStatus.OPEN,
                    lastSeen,
                    pageable
            );
        } else {
            jobs = jobRepository.findLatestJobsWithCompany(
                    JobStatus.OPEN,
                    pageable
            );
        }

        return jobs.stream().map(this::mapToResponse).toList();
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
    // SEARCH JOBS
    // ==================================================
    public PagedJobResponse searchJobs(
            String title,
            String location,
            String companyName,
            Integer minExp,
            Integer maxExp,
            JobType jobType,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Job> jobPage = jobRepository.findAll(
                JobSpecification.searchJobs(
                        title,
                        location,
                        companyName,
                        minExp,
                        maxExp,
                        jobType
                ),
                pageable
        );

        Page<JobResponse> responsePage = jobPage.map(this::mapToResponse);

        return toPagedResponse(responsePage);
    }
    
    public PagedJobResponse quickSearch(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Job> jobPage = jobRepository.quickSearch(
                keyword.toLowerCase(),
                pageable
        );

        return toPagedResponse(jobPage.map(this::mapToResponse));
    }
    
    public PagedJobResponse filterJobs(
            String location,
            JobType jobType,
            Integer minExp,
            Integer maxExp,
            JobStatus status,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobPage = jobRepository.findAll(
                JobSpecification.filterJobs(
                        location,
                        jobType,
                        minExp,
                        maxExp,
                        status
                ),
                pageable
        );

        return toPagedResponse(jobPage.map(this::mapToResponse));
    }
    
    public List<JobResponse> getSuggestedJobs(Long userId) {

        // Fetch last applied job
        Job lastAppliedJob = jobRepository.findLastAppliedJob(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No job history found"));

        return jobRepository.findSuggestedJobs(
                        lastAppliedJob.getLocation(),
                        lastAppliedJob.getIndustry()
                )
                .stream()
                .limit(10)
                .map(this::mapToResponse)
                .toList();
    }

    
    public List<JobResponse> getSimilarJobs(Long jobId) {

        Job job = getJobByIdInternal(jobId);

        return jobRepository.findSimilarJobs(
                        job.getIndustry(),
                        job.getJobType(),
                        job.getId()
                )
                .stream()
                .limit(6)
                .map(this::mapToResponse)
                .toList();
    }
    
    public List<String> getSearchSuggestions(String keyword) {

        return jobRepository.findTitleSuggestions(keyword.toLowerCase());
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
        return jobRepository.findById(jobId)
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
                .jobType(request.getJobType())               // new
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
                .minExperience(job.getMinExperience())   // new
                .maxExperience(job.getMaxExperience())   // new
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)  // new
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
