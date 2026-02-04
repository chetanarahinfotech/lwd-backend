package com.lwd.jobportal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
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
    
    public List<User> getAllUsers() {
        Role role = SecurityUtils.getRole();

        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can block users");
        }
        return userRepository.findAll();
    }

    public void blockUser(Long targetUserId) {

        Long adminId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can block users");
        }

        User user = getUser(targetUserId);

        user.setStatus(UserStatus.BLOCKED);
        user.setIsActive(false);

        // Optional audit
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // 🔎 You can log this
        logAction(adminId, "BLOCK_USER", targetUserId);
    }

    public void unblockUser(Long targetUserId) {

        Long adminId = SecurityUtils.getUserId();

        User user = getUser(targetUserId);
        user.setStatus(UserStatus.ACTIVE);
        user.setIsActive(true);

        userRepository.save(user);
        logAction(adminId, "UNBLOCK_USER", targetUserId);
    }

    // ================= COMPANIES =================

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
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
    
    
    public List<Job> getAllJobs() {
    	Role role = SecurityUtils.getRole();

        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can block users");
        }
        return jobRepository.findAll();
    }

    public void closeJob(Long jobId) {

        Long adminId = SecurityUtils.getUserId();

        Job job = getJob(jobId);
        job.setStatus(JobStatus.CLOSED);

        jobRepository.save(job);
        logAction(adminId, "CLOSE_JOB", jobId);
    }

    // ================= HELPERS =================

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
}
