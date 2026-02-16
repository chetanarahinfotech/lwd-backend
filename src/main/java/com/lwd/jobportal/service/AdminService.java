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
import com.lwd.jobportal.dto.admin.PagedResponse;
import com.lwd.jobportal.dto.admin.UserAdminDTO;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    // ================= USERS =================
    
    public PagedResponse<UserAdminDTO> getAllUsers(int page, int size) {
        validateAdminAccess();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> userPage = userRepository.findAll(pageable);

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



    public void blockUser(Long targetUserId) {
        validateAdminAccess();

        Long adminId = SecurityUtils.getUserId();
        User user = getUser(targetUserId);

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidOperationException("User is already blocked");
        }

        user.setStatus(UserStatus.SUSPENDED);
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        logAction(adminId, "BLOCK_USER", targetUserId);
    }


    public void unblockUser(Long targetUserId) {

        Long adminId = SecurityUtils.getUserId();

        User user = getUser(targetUserId);
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidOperationException("User is already active");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setIsActive(true);

        userRepository.save(user);
        logAction(adminId, "UNBLOCK_USER", targetUserId);
    }
    
    public PagedResponse<RecruiterResponse> getRecruitersByCompanyId(
            Long companyId, int page, int size) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found with id: " + companyId)
                );

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<User> recruiterPage =
                userRepository.findByRoleAndCompany(Role.RECRUITER, company, pageable);

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

    // ================= COMPANIES =================

    public PagedResponse<CompanyAdminDTO> getAllCompanies(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<CompanyAdminDTO> content = companyPage.getContent()
                .stream()
                .map(company -> {

                    User creator = userRepository
                            .findById(company.getCreatedById())
                            .orElse(null);

                    long recruiterCount =
                            userRepository.countByCompanyIdAndRole(
                                    company.getId(), Role.RECRUITER
                            );

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


    
    public void blockCompany(Long companyId) {

        Long adminId = SecurityUtils.getUserId();

        Company company = getCompany(companyId);
        company.setIsActive(false);

        companyRepository.save(company);
        logAction(adminId, "BLOCK_COMPANY", companyId);
    }
    
    
    public void unblockCompany(Long companyId) {
    	
    	Long adminId = SecurityUtils.getUserId();
    	 
        Company company = getCompany(companyId);
        company.setIsActive(true);
        companyRepository.save(company);
        logAction(adminId, "ACTIVE_COMPANY", companyId);
    }


    // ================= JOBS =================
    
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


    // ================= HELPERS =================
    
    private void validateAdminAccess() {
        Role role = SecurityUtils.getRole();
        if (role != Role.ADMIN) {
            throw new ForbiddenActionException("Only ADMIN can perform this action");
        }
    }



    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    private Job getJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private void logAction(Long actorId, String action, Long targetId) {
        // Optional: save into admin_audit table
        System.out.println(
            "ADMIN " + actorId + " performed " + action + " on " + targetId
        );
    }
    
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
