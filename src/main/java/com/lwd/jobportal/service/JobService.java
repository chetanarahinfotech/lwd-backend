package com.lwd.jobportal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.companydto.CompanySummaryDTO;
import com.lwd.jobportal.entity.*;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.jobdto.*;
import com.lwd.jobportal.repository.*;
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
    
    
    // ==================================================
    // RECRUITER CREATE JOB
    // ==================================================
    @PreAuthorize("hasAnyRole('RECRUITER_ADMIN','RECRUITER')")
    public JobResponse createJobAsRecruiter(CreateJobRequest request, Long userId) {
        User recruiter = getUserById(userId);

        Company company = companyRepository.findByCreatedById(userId)
                .orElseThrow(() -> new IllegalArgumentException("You must create a company before posting jobs"));

        Job job = buildJob(request, recruiter, company);
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
