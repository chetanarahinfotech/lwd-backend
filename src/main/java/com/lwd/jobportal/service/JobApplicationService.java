package com.lwd.jobportal.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.dto.companydto.CompanySummaryDTO;
import com.lwd.jobportal.dto.jobapplicationdto.*;
import com.lwd.jobportal.dto.jobdto.JobSummaryDTO;
import com.lwd.jobportal.dto.jobseekerdto.JobSeekerSummaryDTO;
import com.lwd.jobportal.entity.*;
import com.lwd.jobportal.enums.*;
import com.lwd.jobportal.exception.BadRequestException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.exception.UnauthorizedException;
import com.lwd.jobportal.repository.*;
import com.lwd.jobportal.specification.JobApplicationSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobSeekerRepository jobSeekerRepository;

    
    public String applyForJob(JobApplicationRequest request, Long userId) {

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Job is not accepting applications");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (jobApplicationRepository.existsByJobIdAndJobSeekerId(job.getId(), user.getId())) {
            throw new BadRequestException("You already applied for this job");
        }

        // ================= EXTERNAL JOB =================
        if (job.getApplicationSource() == ApplicationSource.EXTERNAL) {

            JobSeeker profile = jobSeekerRepository
                    .findByUserId(userId)
                    .orElseThrow(() ->
                            new BadRequestException("Please complete your job seeker profile first"));

            if (user.getPhone() == null || user.getPhone().isBlank()) {
                throw new BadRequestException("Please add phone number in your profile");
            }

//            if (profile.getResumeUrl() == null || profile.getResumeUrl().isBlank()) {
//                throw new BadRequestException("Please upload your resume before applying");
//            }

            JobApplication application = JobApplication.builder()
                    .job(job)
                    .jobSeeker(user)
                    .applicationSource(ApplicationSource.EXTERNAL)
                    .fullName(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .resumeUrl(profile.getResumeUrl())
                    .externalApplyUrl(job.getExternalApplicationUrl())
                    .status(ApplicationStatus.APPLIED)
                    .appliedAt(LocalDateTime.now())
                    .build();

            jobApplicationRepository.save(application);

            return job.getExternalApplicationUrl();
        }


        // ================= PORTAL JOB =================
        JobApplication application = JobApplication.builder()
                .job(job)
                .jobSeeker(user)
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

        return "Application submitted successfully";
    }


    
    
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getApplicationsByRole(
            Long userId,
            Role role,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("appliedAt").descending()
        );

        Page<JobApplication> applications;

        // ================= ADMIN =================
        if (role == Role.ADMIN) {
            applications = jobApplicationRepository.findAll(pageable);
        }

        // ================= RECRUITER_ADMIN =================
        else if (role == Role.RECRUITER_ADMIN) {

            Company company = companyRepository.findByCreatedById(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Company not found for recruiter admin"));

            applications = jobApplicationRepository
                    .findByJobCompanyId(company.getId(), pageable);
        }

        // ================= RECRUITER =================
        else if (role == Role.RECRUITER) {

            applications = jobApplicationRepository
                    .findByJobCreatedById(userId, pageable);
        }

        // ================= INVALID =================
        else {
            throw new AccessDeniedException("Invalid role");
        }

        return buildPagedResponse(applications);
    }
    
    
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse searchApplications(
            Long userId,
            Role role,
            ApplicationSearchRequest request,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("appliedAt").descending()
        );

        Page<JobApplication> applications = jobApplicationRepository.findAll(
                JobApplicationSpecification.searchApplications(userId, role, request),
                pageable
        );

        return buildPagedResponse(applications);
    }



    // ================= ADMIN: APPLICATIONS BY JOB =================
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getApplicationsByJobId(Long jobId, int page, int size) {
    	if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = jobApplicationRepository.findByJob_Id(jobId, pageable);
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

        application.setStatus(newStatus);
        application.setUpdatedBy(userId); // 🔹 who updated
        jobApplicationRepository.save(application);
    }
    
    // ================= JOB SEEKER: MY APPLICATIONS =================
    // ================= JOB SEEKER: MY APPLICATIONS =================
    @PreAuthorize("hasRole('JOB_SEEKER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getMyApplications(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());

        Page<JobApplication> applications =
                jobApplicationRepository.findByJobSeeker_Id(userId, pageable);

        return buildPagedResponse(applications);
    }

    // ================= HELPER: BUILD PAGED RESPONSE =================
    private PagedApplicationsResponse buildPagedResponse(Page<JobApplication> page) {

        List<Long> userIds = page.getContent().stream()
                .map(JobApplication::getJobSeeker)
                .filter(Objects::nonNull)
                .map(User::getId)
                .distinct()
                .toList();

        Map<Long, JobSeeker> jobSeekerMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : jobSeekerRepository.findByUserIds(userIds).stream()
                        .filter(js -> js.getUser() != null)
                        .collect(Collectors.toMap(
                                js -> js.getUser().getId(),
                                Function.identity()
                        ));

        List<JobApplicationResponse> responses = page.getContent().stream()
                .map(application -> mapToResponse(application, jobSeekerMap))
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
    private JobApplicationResponse mapToResponse(
            JobApplication application,
            Map<Long, JobSeeker> jobSeekerMap
    ) {
        Job job = application.getJob();
        Company company = job != null ? job.getCompany() : null;
        User user = application.getJobSeeker();

        JobSeeker profile = null;
        if (user != null) {
            profile = jobSeekerMap.get(user.getId());
        }

        JobSeekerSummaryDTO jobSeekerDTO = null;
        if (profile != null) {
            jobSeekerDTO = JobSeekerSummaryDTO.builder()
                    .id(user.getId())
                    .headline(profile.getHeadline())
                    .totalExperience(profile.getTotalExperience())
                    .currentCompany(profile.getCurrentCompany())
                    .currentLocation(profile.getCurrentLocation())
                    .currentCTC(profile.getCurrentCTC())
                    .expectedCTC(profile.getExpectedCTC())
                    .immediateJoiner(profile.getImmediateJoiner())
                    .noticePeriod(profile.getNoticePeriod())
                    .resumeUrl(profile.getResumeUrl())
                    .build();
        }

        return JobApplicationResponse.builder()
                .applicationId(application.getId())
                .applicantName(application.getFullName())
                .email(application.getEmail())
                .phone(application.getPhone())
                .applicationSource(application.getApplicationSource())
                .externalApplicationUrl(job != null ? job.getExternalApplicationUrl() : null)
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())

                .jobSeekerId(user != null ? user.getId() : null)
                .jobSeeker(jobSeekerDTO)

                .job(job != null
                        ? JobSummaryDTO.builder()
                                .id(job.getId())
                                .title(job.getTitle())
                                .location(job.getLocation())
                                .jobType(job.getJobType())
                                .minExperience(job.getMinExperience())
                                .maxExperience(job.getMaxExperience())
                                .status(job.getStatus())
                                .createdAt(job.getCreatedAt())
                                .build()
                        : null)

                .company(company != null
                        ? CompanySummaryDTO.builder()
                                .id(company.getId())
                                .companyName(company.getCompanyName())
                                .logo(company.getLogoUrl())
                                .build()
                        : null)

                .build();
    }
}
