package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.jobapplicationdto.JobApplicationResponse;
import com.lwd.jobportal.jobapplicationdto.PagedApplicationsResponse;
import com.lwd.jobportal.jobdto.JobSummaryDTO;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobApplicationRepository;
import com.lwd.jobportal.repository.JobRepository;
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

    // ================= REQUEST COMPANY APPROVAL =================
    @PreAuthorize("hasRole('RECRUITER')")
    public void requestCompanyApproval(Long companyId) {

        Long currentUserId = SecurityUtils.getUserId();

        User recruiter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        // Role check (extra safety)
        if (!SecurityUtils.hasRole(Role.RECRUITER)) {
            throw new AccessDeniedException("Only recruiters can request approval");
        }

        // Already approved
        if (recruiter.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("Recruiter is already approved");
        }

        // Already requested
        if (recruiter.getCompany() != null) {
            throw new IllegalStateException("Approval already requested");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!company.getIsActive()) {
            throw new IllegalStateException("Company is not active");
        }

        recruiter.setCompany(company);
        recruiter.setStatus(UserStatus.PENDING);

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
                jobApplicationRepository.findByJobId(jobId, pageable);

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
}
