package com.lwd.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.authdto.RecruiterResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruiterAdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<RecruiterResponse> getCompanyRecruiters(Long recruiterAdminId) {
        Company company = companyRepository.findByCreatedById(recruiterAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for this admin"));

        List<User> recruiters = userRepository.findByRoleAndCompany(Role.RECRUITER, company);

        return recruiters.stream()
                .map(user -> RecruiterResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }
    
    
    @Transactional(readOnly = true)
    public List<RecruiterResponse> getPendingRecruiters(Long recruiterAdminId) {

        Company company = companyRepository.findByCreatedById(recruiterAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        List<User> recruiters = userRepository
                .findByRoleAndCompanyIdAndStatus(
                        Role.RECRUITER,
                        company.getId(),
                        UserStatus.PENDING
                );

        return recruiters.stream()
                .map(this::mapToResponse)
                .toList();
    }



    @Transactional
    public RecruiterResponse approveRecruiter(Long recruiterId, Long recruiterAdminId) {

        // 1️⃣ Fetch recruiter
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("User is not a recruiter");
        }

        // 2️⃣ Fetch company of logged-in RECRUITER_ADMIN
        Company company = companyRepository.findByCreatedById(recruiterAdminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found for recruiter admin"));

        // 3️⃣ Assign company + activate recruiter
        recruiter.setCompany(company);
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.setIsActive(true);

        userRepository.save(recruiter);

        return mapToResponse(recruiter);
    }


    // ================= BLOCK / UNBLOCK RECRUITER =================
    public RecruiterResponse blockRecruiter(Long recruiterId, boolean block) {

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("User is not a recruiter");
        }

        recruiter.setStatus(block ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        userRepository.save(recruiter);

        return mapToResponse(recruiter);
    }

    // ================= HELPER: MAP USER → DTO =================
    private RecruiterResponse mapToResponse(User user) {
        return RecruiterResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
