package com.lwd.jobportal.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.auth.RefreshTokenService;
import com.lwd.jobportal.companyadmin.RecruiterResponse;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.CompanyStatus;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ForbiddenActionException;
import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.user.UserSpecification;
import com.lwd.jobportal.util.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


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
    private final RefreshTokenService refreshTokenService;

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
    
    public PagedResponse<UserAdminDTO> searchUsers(UserSearchRequestDTO request, int page, int size) {
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
    
    @Transactional
    public void updateUserStatus(Long targetUserId, UserStatus newStatus) {
        validateAdminAccess();

        Long adminId = SecurityUtils.getUserId();
        User user = getUser(targetUserId);

        if (adminId.equals(targetUserId)) {
            throw new InvalidOperationException("You cannot change your own status");
        }

        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenActionException("Super admin status cannot be changed");
        }

        if (newStatus == null) {
            throw new InvalidOperationException("Target status is required");
        }

        UserStatus currentStatus = user.getStatus();
        if (currentStatus == newStatus) {
            throw new InvalidOperationException("User already has status: " + newStatus);
        }

        if (user.getRole() == Role.ADMIN && newStatus == UserStatus.COMPANY_PENDING_APPROVAL) {
            throw new InvalidOperationException("Admin cannot be moved to company pending approval");
        }

        if (user.getRole() == Role.JOB_SEEKER && newStatus == UserStatus.COMPANY_PENDING_APPROVAL) {
            throw new InvalidOperationException("Job seeker cannot be moved to company pending approval");
        }

        user.setStatus(newStatus);

        switch (newStatus) {
            case ACTIVE -> {
                user.setIsActive(true);
                user.setLocked(false);
            }
            case SUSPENDED -> {
                user.setIsActive(false);
                user.setLocked(true);
                refreshTokenService.revokeAllUserTokens(user.getId());
            }
            case PENDING_APPROVAL, COMPANY_PENDING_APPROVAL -> {
                user.setIsActive(false);
                user.setLocked(false);
                refreshTokenService.revokeAllUserTokens(user.getId());
            }
            default -> throw new InvalidOperationException("Unsupported user status: " + newStatus);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        logAction(adminId, "UPDATE_USER_STATUS_TO_" + newStatus.name(), targetUserId);
    }
    
    
    @Transactional
    public void rejectUser(Long targetUserId) {
        validateAdminAccess();

        Long adminId = SecurityUtils.getUserId();
        User user = getUser(targetUserId);

        if (adminId.equals(targetUserId)) {
            throw new InvalidOperationException("You cannot reject your own account");
        }

        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenActionException("Super admin cannot be rejected");
        }

        if (user.getRole() != Role.RECRUITER && user.getRole() != Role.COMPANY_ADMIN) {
            throw new InvalidOperationException("Reject is supported only for recruiter and company admin");
        }

        if (user.getStatus() != UserStatus.PENDING_APPROVAL
                && user.getStatus() != UserStatus.COMPANY_PENDING_APPROVAL) {
            throw new InvalidOperationException("User is not in rejectable state");
        }

        user.setCompany(null);
        user.setStatus(UserStatus.ACTIVE);
        user.setIsActive(true);
        user.setLocked(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(user.getId());

        logAction(adminId, "REJECT_USER", targetUserId);
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

        List<Company> companies = companyPage.getContent();

        if (companies.isEmpty()) {
            return new PagedResponse<>(
                    List.of(),
                    companyPage.getNumber(),
                    companyPage.getSize(),
                    companyPage.getTotalElements(),
                    companyPage.getTotalPages(),
                    companyPage.isLast()
            );
        }

        List<Long> companyIds = companies.stream()
                .map(Company::getId)
                .toList();

        Map<Long, Long> recruiterCountMap = userRepository.countByCompanyIdsAndRole(companyIds, Role.RECRUITER)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Long> CompanyAdminCountMap = userRepository.countByCompanyIdsAndRole(companyIds, Role.COMPANY_ADMIN)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Long> jobCountMap = jobRepository.countJobsByCompanyIds(companyIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        List<CompanyAdminDTO> content = companies.stream()
                .map(company -> {
                    User creator = company.getCreatedBy();

                    return CompanyAdminDTO.builder()
                            .id(company.getId())
                            .companyName(company.getCompanyName())
                            .status(company.getStatus())
                            .verified(company.getVerified())
                            .createdById(creator != null ? creator.getId() : null)
                            .createdByName(creator != null ? creator.getName() : "N/A")
                            .totalRecruiters(recruiterCountMap.getOrDefault(company.getId(), 0L))
                            .totalCompanyAdmins(CompanyAdminCountMap.getOrDefault(company.getId(), 0L))
                            .totalJobs(jobCountMap.getOrDefault(company.getId(), 0L))
                            .createdAt(company.getCreatedAt())
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
    
    public void updateCompanyStatus(Long companyId, CompanyStatus newStatus) {

        Long adminId = SecurityUtils.getUserId();
        Company company = getCompany(companyId);

        CompanyStatus currentStatus = company.getStatus();

        // 🔒 Optional: Add business rules
        validateStatusTransition(currentStatus, newStatus);

        company.setStatus(newStatus);
        companyRepository.save(company);

        logAction(adminId, "UPDATE_COMPANY_STATUS_" + newStatus, companyId);
    }
    
    private void validateStatusTransition(CompanyStatus current, CompanyStatus target) {

        if (current == target) {
            throw new InvalidOperationException("Company already in status: " + target);
        }

        switch (current) {
            case PENDING -> {
                if (target != CompanyStatus.ACTIVE && target != CompanyStatus.REJECTED) {
                    throw new InvalidOperationException("Invalid transition from PENDING");
                }
            }
            case ACTIVE -> {
                if (target != CompanyStatus.SUSPENDED) {
                    throw new InvalidOperationException("Only ACTIVE → SUSPENDED allowed");
                }
            }
            case SUSPENDED -> {
                if (target != CompanyStatus.ACTIVE) {
                    throw new InvalidOperationException("Only SUSPENDED → ACTIVE allowed");
                }
            }
            case REJECTED -> {
                if (target != CompanyStatus.PENDING) {
                    throw new InvalidOperationException("Only REJECTED → PENDING allowed");
                }
            }
        }
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
