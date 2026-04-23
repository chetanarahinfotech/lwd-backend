package com.lwd.jobportal.jobapplication;

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

import com.lwd.jobportal.company.CompanySummaryDTO;
import com.lwd.jobportal.entity.*;
import com.lwd.jobportal.enums.*;
import com.lwd.jobportal.exception.BadRequestException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.exception.UnauthorizedException;
import com.lwd.jobportal.job.JobSummaryDTO;
import com.lwd.jobportal.jobseeker.JobSeekerSummaryDTO;
import com.lwd.jobportal.notification.CreateNotificationRequest;
import com.lwd.jobportal.notification.NotificationPriority;
import com.lwd.jobportal.notification.NotificationService;
import com.lwd.jobportal.notification.NotificationType;
import com.lwd.jobportal.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final NotificationService notificationService;

    
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

            JobApplication saved = jobApplicationRepository.save(application);
            sendApplicationCreatedNotifications(saved, userId);

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

        JobApplication saved = jobApplicationRepository.save(application);
        sendApplicationCreatedNotifications(saved, userId);
        return "Application submitted successfully";
    }


    
    
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY_ADMIN','RECRUITER')")
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

     // ================= COMPANY_ADMIN =================
        else if (role == Role.COMPANY_ADMIN) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Company company = user.getCompany();

            if (company == null) {
                throw new ResourceNotFoundException("Recruiter admin is not assigned to any company");
            }

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
    
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY_ADMIN','RECRUITER')")
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

        Long companyId = null;

        if (role == Role.COMPANY_ADMIN) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (user.getCompany() == null) {
                throw new ResourceNotFoundException("Recruiter admin is not assigned to any company");
            }

            companyId = user.getCompany().getId();
        }

        Page<JobApplication> applications = jobApplicationRepository.findAll(
                JobApplicationSpecification.searchApplications(userId, companyId, role, request),
                pageable
        );

        return buildPagedResponse(applications);
    }



    // ================= ADMIN: APPLICATIONS BY JOB =================
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY_ADMIN','RECRUITER')")
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getApplicationsByJobId(Long jobId, int page, int size) {
    	if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = jobApplicationRepository.findByJob_Id(jobId, pageable);
        return buildPagedResponse(applications);
    }
    
    
    @PreAuthorize("hasAnyRole('ADMIN','COMPANY_ADMIN','RECRUITER')")
    @Transactional
    public void changeApplicationStatus(
            Long applicationId,
            ApplicationStatus newStatus,
            Long userId,
            Role role
    ) {
        JobApplication application = jobApplicationRepository.findWithJobAndJobSeekerById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));


        if (application.getStatus() == newStatus) {
            return;
        }
        application.setStatus(newStatus);
        application.setUpdatedBy(userId);

        JobApplication saved = jobApplicationRepository.save(application);

        sendApplicationStatusNotification(saved, userId);
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
    
    private void sendApplicationCreatedNotifications(JobApplication application, Long actorUserId) {

        sendApplicationSubmittedNotification(application, actorUserId);
        sendNewApplicationNotification(application, actorUserId);
    }
    
    
    private void sendApplicationSubmittedNotification(JobApplication application, Long actorUserId) {

        if (application == null || application.getJobSeeker() == null) {
            return;
        }

        String jobTitle = application.getJob() != null
                ? application.getJob().getTitle()
                : "the job";

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(application.getJobSeeker().getId())
                .type(NotificationType.APPLICATION_SUBMITTED)
                .priority(NotificationPriority.MEDIUM)
                .title("Application Submitted")
                .message("You have successfully applied for " + jobTitle)
                .actionUrl("/my-applications")
                .referenceId(application.getId())
                .referenceType("JOB_APPLICATION")
                .build();

        notificationService.createNotification(request, actorUserId);
    }
    
    
    private void sendNewApplicationNotification(JobApplication application, Long actorUserId) {

        if (application == null || application.getJob() == null || application.getJob().getCreatedBy().getId() == null) {
            return;
        }

        String jobTitle = application.getJob().getTitle();
        String candidateName = application.getJobSeeker() != null
                ? application.getJobSeeker().getName()
                : "A candidate";

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(application.getJob().getCreatedBy().getId())
                .type(NotificationType.NEW_APPLICATION_RECEIVED)
                .priority(NotificationPriority.HIGH)
                .title("New Application Received")
                .message(candidateName + " applied for your job: " + jobTitle)
                .actionUrl("/recruiter/applications/" + application.getId())
                .referenceId(application.getId())
                .referenceType("JOB_APPLICATION")
                .build();

        notificationService.createNotification(request, actorUserId);
    }
    
    private void sendApplicationStatusNotification(JobApplication application, Long actorUserId) {
        if (application == null || application.getJobSeeker() == null || application.getJobSeeker().getId() == null) {
            return;
        }

        String jobTitle = application.getJob() != null ? application.getJob().getTitle() : "your application";

        String title = "Application Status Updated";
        String message = buildApplicationStatusMessage(application.getStatus(), jobTitle);

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(application.getJobSeeker().getId())
                .type(NotificationType.APPLICATION_STATUS_CHANGED)
                .priority(resolveNotificationPriority(application.getStatus()))
                .title(title)
                .message(message)
                .actionUrl("/my-applications")
                .referenceId(application.getId())
                .referenceType("JOB_APPLICATION")
                .build();

        notificationService.createNotification(request, actorUserId);
    }
    
    
    private String buildApplicationStatusMessage(ApplicationStatus status, String jobTitle) {
        return switch (status) {
            case SHORTLISTED -> "Good news! Your application for " + jobTitle + " has been shortlisted.";
            case INTERVIEW_SCHEDULED -> "Your application for " + jobTitle + " has moved to the interview stage.";
            case SELECTED -> "Congratulations! You have been selected for " + jobTitle + ".";
            case HIRED -> "Congratulations! You have been hired for " + jobTitle + ".";
            case REJECTED -> "Your application for " + jobTitle + " was rejected.";
            case APPLIED -> "Your application for " + jobTitle + " is marked as applied.";
            default -> "Your application status for " + jobTitle + " has been updated.";
        };
    }
    
    private NotificationPriority resolveNotificationPriority(ApplicationStatus status) {
        return switch (status) {
            case HIRED, SELECTED, INTERVIEW_SCHEDULED, SHORTLISTED -> NotificationPriority.HIGH;
            case REJECTED -> NotificationPriority.MEDIUM;
            default -> NotificationPriority.LOW;
        };
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
                .updatedAt(application.getUpdatedAt())
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
