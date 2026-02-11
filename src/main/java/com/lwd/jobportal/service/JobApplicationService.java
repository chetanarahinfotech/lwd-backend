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
import com.lwd.jobportal.enums.*;
import com.lwd.jobportal.exception.BadRequestException;
import com.lwd.jobportal.exception.ForbiddenActionException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.exception.UnauthorizedException;
import com.lwd.jobportal.jobapplicationdto.*;
import com.lwd.jobportal.jobdto.JobSummaryDTO;
import com.lwd.jobportal.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    
    public void applyForJob(JobApplicationRequest request, Long jobSeekerId) {

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Job is not accepting applications");
        }

        User jobSeeker = userRepository.findById(jobSeekerId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (jobApplicationRepository
                .existsByJobIdAndJobSeekerId(job.getId(), jobSeeker.getId())) {
            throw new BadRequestException("You have already applied for this job");
        }

        JobApplication application = JobApplication.builder()
                .job(job)
                .jobSeeker(jobSeeker)
                .applicationSource(ApplicationSource.PORTAL)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .skills(request.getSkills())
                .coverLetter(request.getCoverLetter())
                .resumeUrl(request.getResumeUrl())
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDateTime.now())
                .build();

        jobApplicationRepository.save(application);
    }


    // ================= ADMIN: APPLICATIONS BY JOB =================
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getApplicationsByJobAdmin(Long jobId, int page, int size) {
    	if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = jobApplicationRepository.findByJobId(jobId, pageable);
        return buildPagedResponse(applications);
    }

    // ================= COMPANY ADMIN / RECRUITER: APPLICATIONS BY JOB =================
    @PreAuthorize("hasAnyRole('RECRUITER_ADMIN','RECRUITER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getApplicationsByJobCompany(
            Long jobId, Long userId, int page, int size) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = companyRepository.findByCreatedById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for this user"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCompany().getId().equals(company.getId())) {
        	throw new ForbiddenActionException(
        	        "You are not allowed to view applications for this job"
        	);

        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications =
                jobApplicationRepository.findByJobIdAndJobCompanyId(jobId, company.getId(), pageable);

        return buildPagedResponse(applications);
    }


    // ================= JOB SEEKER: MY APPLICATIONS =================
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getMyApplications(Long jobSeekerId, int page, int size) {
    	if (!userRepository.existsById(jobSeekerId)) {
            throw new ResourceNotFoundException("Job seeker not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = jobApplicationRepository.findByJobSeekerId(jobSeekerId, pageable);
        return buildPagedResponse(applications);
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @Transactional
    public void changeApplicationStatus(
            Long applicationId,
            ApplicationStatus newStatus,
            Long userId,
            Role role
    ) {

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        // ADMIN → allow directly
        if (role == Role.ADMIN) {
            application.setStatus(newStatus);
            application.setUpdatedBy(userId); 
            jobApplicationRepository.save(application);
            return;
        }

        Company company = companyRepository.findByCreatedById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!company.getId().equals(application.getJob().getCompany().getId())) {
            throw new AccessDeniedException("You are not allowed to update this application");
        }

        application.setStatus(newStatus);
        application.setUpdatedBy(userId); // 🔹 who updated
        jobApplicationRepository.save(application);
    }



    // ================= COMPANY ADMIN / RECRUITER: ALL COMPANY APPLICATIONS =================
    @PreAuthorize("hasAnyRole('RECRUITER_ADMIN','RECRUITER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getMyCompanyApplications(Long userId, int page, int size) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = companyRepository.findByCreatedById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for this user"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = jobApplicationRepository.findByJobCompanyId(company.getId(), pageable);

        return buildPagedResponse(applications);
    }
    
    

    // ================= HELPER: BUILD PAGED RESPONSE =================
    private PagedApplicationsResponse buildPagedResponse(Page<JobApplication> page) {
        List<JobApplicationResponse> responses = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PagedApplicationsResponse.builder()
                .applications(responses)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ================= HELPER: MAP ENTITY → DTO =================
    private JobApplicationResponse mapToResponse(JobApplication application) {

        Job job = application.getJob();
        Company company = job.getCompany();

        return JobApplicationResponse.builder()
                .applicationId(application.getId())
                .applicantName(application.getFullName())
                .email(application.getEmail())
                .phone(application.getPhone())
                .applicationSource(application.getApplicationSource())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .job(JobSummaryDTO.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .location(job.getLocation())
                        .jobType(job.getJobType())
                        .minExperience(job.getMinExperience())
                        .maxExperience(job.getMaxExperience())
                        .status(job.getStatus())
                        .createdAt(job.getCreatedAt())
                        .build())
                .company(CompanySummaryDTO.builder()
                        .id(company.getId())
                        .companyName(company.getCompanyName())
                        .logo(company.getLogoUrl())
                        .build())
                .build();
    }
}
