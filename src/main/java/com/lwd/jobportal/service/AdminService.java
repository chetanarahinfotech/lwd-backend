package com.lwd.jobportal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.admin.CompanyAdminDTO;
import com.lwd.jobportal.dto.admin.JobAdminDTO;
import com.lwd.jobportal.dto.admin.UserAdminDTO;
import com.lwd.jobportal.dto.admin.UserSearchRequest;
import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterResponse;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ForbiddenActionException;
import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.specification.UserSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * ============================================================
 * AdminService
 * ============================================================
 *
 * This service handles all administrative operations in the system.
 *
 * Responsibilities:
 *  - Manage Users (block/unblock/view)
 *  - Manage Companies (block/unblock/view)
 *  - Manage Jobs (close/view)
 *  - Retrieve Recruiters under a Company
 *
 * Security:
 *  - Only users with ADMIN role are allowed to perform these actions.
 *
 * Transactional:
 *  - All operations are wrapped in a transactional context.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    // ================= REPOSITORIES =================

    // Repository for User database operations
    private final UserRepository userRepository;

    // Repository for Company database operations
    private final CompanyRepository companyRepository;

    // Repository for Job database operations
    private final JobRepository jobRepository;

    // ============================================================
    // ========================== USERS ============================
    // ============================================================

    /**
     * Fetch paginated list of all users in the system.
     *
     * @param page page number (0-based)
     * @param size number of records per page
     * @return paginated response containing UserAdminDTO
     */
    public PagedResponse<UserAdminDTO> getAllUsers(int page, int size) {
        validateAdminAccess(); // Ensure only ADMIN can access

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> userPage = userRepository.findAll(pageable);

        // Convert User entity to UserAdminDTO
        List<UserAdminDTO> content = userPage.getContent()
                .stream()
                .map(this::toUserAdminDTO)
                .toList();

        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }
    
    public PagedResponse<UserAdminDTO> searchUsers(UserSearchRequest request, int page, int size) {
        validateAdminAccess();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<User> userPage = userRepository.findAll(
                UserSpecification.searchUsers(request),
                pageable
        );

        List<UserAdminDTO> content = userPage.getContent()
                .stream()
                .map(this::toUserAdminDTO)
                .toList();

        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    /**
     * Block (suspend) a user.
     * Changes status to SUSPENDED and disables account.
     */
    public void blockUser(Long targetUserId) {
        validateAdminAccess();

        Long adminId = SecurityUtils.getUserId();
        User user = getUser(targetUserId);

        // Prevent blocking already suspended users
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidOperationException("User is already blocked");
        }

        user.setStatus(UserStatus.SUSPENDED);
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Log admin action
        logAction(adminId, "BLOCK_USER", targetUserId);
    }

    /**
     * Unblock (activate) a user.
     */
    public void unblockUser(Long targetUserId) {

        Long adminId = SecurityUtils.getUserId();
        User user = getUser(targetUserId);

        // Prevent activating already active users
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidOperationException("User is already active");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setIsActive(true);

        userRepository.save(user);

        logAction(adminId, "UNBLOCK_USER", targetUserId);
    }

    /**
     * Fetch all recruiters belonging to a specific company.
     */
    public PagedResponse<RecruiterResponse> getRecruitersByCompanyId(
            Long companyId, int page, int size) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found with id: " + companyId)
                );

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Fetch only users with RECRUITER role for this company
        Page<User> recruiterPage =
                userRepository.findByRoleAndCompany(Role.RECRUITER, company, pageable);

        // Map to RecruiterResponse DTO
        List<RecruiterResponse> content = recruiterPage.getContent()
                .stream()
                .map(user -> RecruiterResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();

        return new PagedResponse<>(
                content,
                recruiterPage.getNumber(),
                recruiterPage.getSize(),
                recruiterPage.getTotalElements(),
                recruiterPage.getTotalPages(),
                recruiterPage.isLast()
        );
    }

    // ============================================================
    // ======================== COMPANIES =========================
    // ============================================================

    /**
     * Fetch paginated list of all companies with recruiter count and job count.
     */
    public PagedResponse<CompanyAdminDTO> getAllCompanies(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<CompanyAdminDTO> content = companyPage.getContent()
                .stream()
                .map(company -> {

                    // Fetch company creator details
                    User creator = userRepository
                            .findById(company.getCreatedById())
                            .orElse(null);

                    // Count recruiters under company
                    long recruiterCount =
                            userRepository.countByCompanyIdAndRole(
                                    company.getId(), Role.RECRUITER
                            );

                    // Count jobs under company
                    long jobCount =
                            jobRepository.countByCompanyId(company.getId());

                    return CompanyAdminDTO.builder()
                            .id(company.getId())
                            .companyName(company.getCompanyName())
                            .isActive(company.getIsActive())
                            .createdById(company.getCreatedById())
                            .createdByName(
                                    creator != null ? creator.getName() : "N/A"
                            )
                            .totalRecruiters(recruiterCount)
                            .totalJobs(jobCount)
                            .build();
                })
                .toList();

        return new PagedResponse<>(
                content,
                companyPage.getNumber(),
                companyPage.getSize(),
                companyPage.getTotalElements(),
                companyPage.getTotalPages(),
                companyPage.isLast()
        );
    }

    /**
     * Deactivate (block) a company.
     */
    public void blockCompany(Long companyId) {

        Long adminId = SecurityUtils.getUserId();
        Company company = getCompany(companyId);

        company.setIsActive(false);

        companyRepository.save(company);

        logAction(adminId, "BLOCK_COMPANY", companyId);
    }

    /**
     * Activate (unblock) a company.
     */
    public void unblockCompany(Long companyId) {

        Long adminId = SecurityUtils.getUserId();
        Company company = getCompany(companyId);

        company.setIsActive(true);

        companyRepository.save(company);

        logAction(adminId, "ACTIVE_COMPANY", companyId);
    }

    // ============================================================
    // =========================== JOBS ===========================
    // ============================================================

    /**
     * Fetch paginated list of all jobs.
     */
    public PagedResponse<JobAdminDTO> getAllJobs(int page, int size) {

        validateAdminAccess();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Job> jobPage = jobRepository.findAll(pageable);

        List<JobAdminDTO> content = jobPage.getContent()
                .stream()
                .map(job -> JobAdminDTO.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .location(job.getLocation())
                        .status(job.getStatus())
                        .build())
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

    /**
     * Close a job (ADMIN action).
     */
    public void closeJob(Long jobId) {
        validateAdminAccess();

        Long adminId = SecurityUtils.getUserId();
        Job job = getJob(jobId);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new InvalidOperationException("Job is already closed");
        }

        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);

        logAction(adminId, "CLOSE_JOB", jobId);
    }

    // ============================================================
    // ========================== HELPERS =========================
    // ============================================================

    /**
     * Ensures that only ADMIN users can access protected methods.
     */
    private void validateAdminAccess() {
        Role role = SecurityUtils.getRole();
        if (role != Role.ADMIN) {
            throw new ForbiddenActionException("Only ADMIN can perform this action");
        }
    }

    /**
     * Fetch user by ID or throw exception.
     */
    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Fetch company by ID or throw exception.
     */
    private Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    /**
     * Fetch job by ID or throw exception.
     */
    private Job getJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    /**
     * Logs admin actions.
     * (In production, this should be stored in an audit table.)
     */
    private void logAction(Long actorId, String action, Long targetId) {
        System.out.println(
            "ADMIN " + actorId + " performed " + action + " on " + targetId
        );
    }

    /**
     * Converts User entity to UserAdminDTO.
     */
    private UserAdminDTO toUserAdminDTO(User user) {
        return UserAdminDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(user.getIsActive())
                .build();
    }
}
