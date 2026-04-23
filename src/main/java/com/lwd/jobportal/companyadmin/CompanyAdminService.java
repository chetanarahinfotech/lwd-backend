package com.lwd.jobportal.companyadmin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.auth.RefreshTokenService;
import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ForbiddenActionException;
import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyAdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public PagedResponse<RecruiterResponse> getCompanyRecruiters(
            Long CompanyAdminId,
            int page,
            int size
    ) {

        Company company = companyRepository.findByCreatedBy_Id(CompanyAdminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found for this admin")
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



    
    @Transactional(readOnly = true)
    public PagedResponse<RecruiterResponse> getPendingRecruiters(
            Long CompanyAdminId,
            int page,
            int size
    ) {

        Company company = companyRepository.findByCreatedBy_Id(CompanyAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<User> recruiterPage = userRepository
                .findByRoleAndCompanyIdAndStatus(
                        Role.RECRUITER,
                        company.getId(),
                        UserStatus.COMPANY_PENDING_APPROVAL,
                        pageable
                );

        List<RecruiterResponse> content = recruiterPage.getContent()
                .stream()
                .map(this::mapToResponse)
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
    @Transactional
    public void updateRecruiterStatus(Long recruiterId, UserStatus newStatus) {
        Long actorId = SecurityUtils.getUserId();

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));

        if (actor.getRole() != Role.COMPANY_ADMIN) {
            throw new ForbiddenActionException("Only company admin can update recruiter status");
        }

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new InvalidOperationException("Target user is not a recruiter");
        }

        Company actorCompany = companyRepository.findByCreatedBy_Id(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for company admin"));

        if (recruiter.getCompany() == null || !recruiter.getCompany().getId().equals(actorCompany.getId())) {
            throw new ForbiddenActionException("Recruiter does not belong to your company");
        }

        if (recruiter.getStatus() == newStatus) {
            throw new InvalidOperationException("Recruiter already has status: " + newStatus);
        }

        // Company admin can only move recruiter among these statuses
        if (newStatus != UserStatus.ACTIVE &&
            newStatus != UserStatus.SUSPENDED &&
            newStatus != UserStatus.COMPANY_PENDING_APPROVAL) {
            throw new InvalidOperationException("Company admin cannot assign this status");
        }

        recruiter.setStatus(newStatus);

        switch (newStatus) {
            case ACTIVE -> {
                recruiter.setIsActive(true);
                recruiter.setLocked(false);
            }
            case SUSPENDED -> {
                recruiter.setIsActive(false);
                recruiter.setLocked(true);
                refreshTokenService.revokeAllUserTokens(recruiter.getId());
            }
            case COMPANY_PENDING_APPROVAL -> {
                recruiter.setIsActive(false);
                recruiter.setLocked(false);
                refreshTokenService.revokeAllUserTokens(recruiter.getId());
            }
            default -> throw new InvalidOperationException("Unsupported recruiter status");
        }

        recruiter.setUpdatedAt(LocalDateTime.now());
        userRepository.save(recruiter);
    }
    
    @Transactional
    public UserApprovalResponse rejectRecruiter(Long recruiterId) {
        Long actorId = SecurityUtils.getUserId();

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));

        if (actor.getRole() != Role.COMPANY_ADMIN) {
            throw new ForbiddenActionException("Only company admin can reject recruiter");
        }

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new InvalidOperationException("Target user is not a recruiter");
        }

        if (recruiter.getStatus() != UserStatus.COMPANY_PENDING_APPROVAL) {
            throw new InvalidOperationException("Recruiter is not waiting for company approval");
        }

        Company actorCompany = companyRepository.findByCreatedBy_Id(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for company admin"));

        if (recruiter.getCompany() == null || !recruiter.getCompany().getId().equals(actorCompany.getId())) {
            throw new ForbiddenActionException("Recruiter does not belong to your company approval queue");
        }

        recruiter.setCompany(null);
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.setIsActive(true);
        recruiter.setLocked(false);
        recruiter.setUpdatedAt(LocalDateTime.now());

        userRepository.save(recruiter);

        return mapToUserApprovalResponse(recruiter);
    }
    
    private UserApprovalResponse mapToUserApprovalResponse(User user) {
        return UserApprovalResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .isActive(user.getIsActive())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getCompanyName() : null)
                .build();
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
