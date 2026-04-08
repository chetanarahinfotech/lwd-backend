package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.dto.jobapplicationdto.JobApplicationResponse;
import com.lwd.jobportal.dto.jobapplicationdto.PagedApplicationsResponse;
import com.lwd.jobportal.dto.jobapplicationdto.RecentApplicationDTO;
import com.lwd.jobportal.dto.jobdto.JobStatsDTO;
import com.lwd.jobportal.dto.jobdto.JobSummaryDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterProfileSummaryDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterRequestDTO;
import com.lwd.jobportal.dto.recruiterdto.RecruiterResponseDTO;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.entity.Recruiter;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.ApplicationStatus;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.RecruiterRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruiterService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final RecruiterRepository recruiterRepository;

    // =====================================================
    // CREATE OR UPDATE PROFILE
    // =====================================================

    public RecruiterResponseDTO createOrUpdateProfile(RecruiterRequestDTO dto) {

        if (!SecurityUtils.hasRole(Role.RECRUITER)) {
            throw new AccessDeniedException("Only Recruiters can update profile");
        }

        Long userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Recruiter recruiter = recruiterRepository
                .findByUserId(userId)
                .orElse(null);

        if (recruiter == null) {
            recruiter = new Recruiter();
            recruiter.setUser(user);
        }

        updateFields(recruiter, dto);

        Recruiter saved = recruiterRepository.save(recruiter);

        return mapToDTO(saved);
    }

    // =====================================================
    // GET MY PROFILE
    // =====================================================

    public RecruiterResponseDTO getMyProfile() {

        if (!SecurityUtils.hasRole(Role.RECRUITER)) {
            throw new AccessDeniedException("Only Recruiters can access profile");
        }

        Long userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Recruiter recruiter = recruiterRepository
                .findByUserId(userId)
                .orElse(null);

        if (recruiter == null) {
            recruiter = new Recruiter();
            recruiter.setUser(user);

            recruiter = recruiterRepository.save(recruiter);
        }

        return mapToDTO(recruiter);
    }

    // =====================================================
    // GET RECRUITER PROFILE BY USER ID
    // =====================================================

    public RecruiterResponseDTO getRecruiterByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Recruiter recruiter = recruiterRepository
                .findByUserId(userId)
                .orElse(null);

        if (recruiter == null) {
            recruiter = new Recruiter();
            recruiter.setUser(user);

            recruiter = recruiterRepository.save(recruiter);
        }

        return mapToDTO(recruiter);
    }

    // =====================================================
    // UPDATE FIELDS
    // =====================================================

    private void updateFields(Recruiter recruiter, RecruiterRequestDTO dto) {

        recruiter.setDesignation(dto.getDesignation());
        recruiter.setExperience(dto.getExperience());
        recruiter.setLocation(dto.getLocation());
        recruiter.setPhone(dto.getPhone());
        recruiter.setLinkedinUrl(dto.getLinkedinUrl());
        recruiter.setAbout(dto.getAbout());

    }

    // =====================================================
    // MAP ENTITY TO DTO
    // =====================================================

    private RecruiterResponseDTO mapToDTO(Recruiter recruiter) {

        return RecruiterResponseDTO.builder()
                .id(recruiter.getId())
                .userId(recruiter.getUser().getId())
                .designation(recruiter.getDesignation())
                .experience(recruiter.getExperience())
                .location(recruiter.getLocation())
                .phone(recruiter.getPhone())
                .linkedinUrl(recruiter.getLinkedinUrl())
                .about(recruiter.getAbout())
                .profileCompletion(recruiter.getProfileCompletion())
                .createdAt(recruiter.getCreatedAt())
                .updatedAt(recruiter.getUpdatedAt())
                .build();
    }

    // ================= REQUEST COMPANY APPROVAL =================
    @PreAuthorize("hasRole('RECRUITER')")
    public void requestCompanyApproval(Long companyId) {

        Long currentUserId = SecurityUtils.getUserId();

        User recruiter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        // Role check (extra safety)
        if (!SecurityUtils.hasRole(Role.RECRUITER)) {
            throw new AccessDeniedException("Only recruiters can send request for approval");
        }

        // Already approved
//        if (recruiter.getStatus() == UserStatus.ACTIVE) {
//            throw new IllegalStateException("Recruiter is already approved.");
//        }

        // Company already assigned / request already made
        if (recruiter.getCompany() != null) {
            throw new IllegalStateException("Company already assigned or approval request already sent.");
        }


        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!company.getIsActive()) {
            throw new IllegalStateException("Company is not active");
        }

        recruiter.setCompany(company);
        recruiter.setStatus(UserStatus.PENDING_APPROVAL);

        userRepository.save(recruiter);
    }

    // ================= GET MY JOBS =================
    @Transactional(readOnly = true)
    public List<JobSummaryDTO> getMyPostedJobs() {

        User recruiter = validateActiveRecruiter();

        return jobRepository.findByCreatedById(recruiter.getId())
                .stream()
                .map(job -> JobSummaryDTO.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .location(job.getLocation())
                        .jobType(job.getJobType())
                        .minExperience(job.getMinExperience())
                        .maxExperience(job.getMaxExperience())
                        .status(job.getStatus())
                        .createdAt(job.getCreatedAt())
                        .build())
                .toList();
    }

    // ================= GET APPLICATIONS =================
    @Transactional(readOnly = true)
    public PagedApplicationsResponse getApplicationsForJob(
            Long jobId, int page, int size) {

        User recruiter = validateActiveRecruiter();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getCreatedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("This job does not belong to you");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications =
                jobApplicationRepository.findByJob_Id(jobId, pageable);

        return PagedApplicationsResponse.builder()
                .applications(applications.getContent().stream()
                        .map(app -> JobApplicationResponse.builder()
                                .applicationId(app.getId())
                                .applicantName(app.getFullName())
                                .email(app.getEmail())
                                .phone(app.getPhone())
                                .status(app.getStatus())
                                .appliedAt(app.getAppliedAt())
                                .build())
                        .toList())
                .currentPage(applications.getNumber())
                .pageSize(applications.getSize())
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .last(applications.isLast())
                .build();
    }

    // ================= HELPER =================
    private User validateActiveRecruiter() {

        Long userId = SecurityUtils.getUserId();

        User recruiter = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (!SecurityUtils.hasRole(Role.RECRUITER)) {
            throw new AccessDeniedException("User is not a recruiter");
        }

        if (recruiter.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("Recruiter not approved yet");
        }

        if (recruiter.getCompany() == null) {
            throw new AccessDeniedException("Recruiter has no company assigned");
        }

        return recruiter;
    }
    
    
    public RecruiterProfileSummaryDTO getRecruiterSummary(Long recruiterId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruiter not found"));

        RecruiterProfileSummaryDTO dto = new RecruiterProfileSummaryDTO();

        // Basic info
        dto.setName(recruiter.getUser().getName());
        dto.setDesignation(recruiter.getDesignation());
        dto.setExperience(recruiter.getExperience());
        dto.setLocation(recruiter.getLocation());
        dto.setPhone(recruiter.getPhone());
        dto.setLinkedinUrl(recruiter.getLinkedinUrl());
        dto.setAbout(recruiter.getAbout());

        // Summary metrics
        dto.setMyPostedJobs(jobRepository.countByCreatedById(recruiterId));
        dto.setMyActiveJobs(jobRepository.countByCreatedByIdAndStatus(recruiterId, JobStatus.OPEN));
        dto.setTotalApplications(jobApplicationRepository.countByJobCreatedById(recruiterId));
        dto.setInterviewsScheduled(jobApplicationRepository.countByJobCreatedByIdAndStatus(recruiterId, ApplicationStatus.INTERVIEW_SCHEDULED));
        dto.setShortlistedCandidates(jobApplicationRepository.countByJobCreatedByIdAndStatus(recruiterId, ApplicationStatus.SHORTLISTED));

        // Per-job stats
        dto.setPerJobStats(jobRepository.findByCreatedById(recruiterId).stream()
                .map(this::mapJobStats)
                .collect(Collectors.toList()));

        // Recent applications (last 5)
        dto.setRecentApplications(jobApplicationRepository.findTop5ByJobCreatedByIdOrderByAppliedAtDesc(recruiterId)
                .stream()
                .map(this::mapRecentApplication)
                .collect(Collectors.toList()));

        return dto;
    }

    // ==================== PRIVATE HELPERS ====================
    private JobStatsDTO mapJobStats(Job job) {

        Long jobId = job.getId();

        long applications = jobApplicationRepository.countByJobId(jobId);

        long shortlisted = jobApplicationRepository.countByJobIdAndStatus(
                jobId,
                ApplicationStatus.SHORTLISTED
        );

        long rejected = jobApplicationRepository.countByJobIdAndStatus(
                jobId,
                ApplicationStatus.REJECTED
        );

        long pending = jobApplicationRepository.countByJobIdAndStatusIn(
                jobId,
                List.of(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW_SCHEDULED)
        );

        long interview = jobApplicationRepository.countByJobIdAndStatus(
                jobId,
                ApplicationStatus.INTERVIEW_SCHEDULED
        );

        return JobStatsDTO.builder()
                .jobId(jobId)
                .jobTitle(job.getTitle())
                .applications(applications)
                .shortlisted(shortlisted)
                .rejected(rejected)
                .pending(pending)
                .interview(interview)
                .build();
    }

    private RecentApplicationDTO mapRecentApplication(JobApplication app) {

        String candidateName;

        if (app.getFullName() != null && !app.getFullName().isEmpty()) {
            candidateName = app.getFullName();
        } else if (app.getJobSeeker() != null) {
            candidateName = app.getJobSeeker().getName();
        } else {
            candidateName = "Unknown";
        }

        return RecentApplicationDTO.builder()
                .applicationId(app.getId())
                .candidateName(candidateName)
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : null)
                .appliedDate(app.getAppliedAt() != null
                        ? app.getAppliedAt().toLocalDate().toString()
                        : null)
                .status(app.getStatus() != null ? app.getStatus().name() : null)
                .applicationSource(
                        app.getApplicationSource() != null
                                ? app.getApplicationSource().name()
                                : "PORTAL"
                )
                .build();
    }
}
