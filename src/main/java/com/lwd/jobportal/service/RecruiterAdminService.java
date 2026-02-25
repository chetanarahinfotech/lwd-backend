package com.lwd.jobportal.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.dto.comman.PagedResponse;
import com.lwd.jobportal.dto.companydto.CompanySummaryDTO;
import com.lwd.jobportal.dto.jobdto.JobResponse;
import com.lwd.jobportal.dto.recruiteradmindto.RecruiterResponse;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.JobRepository;
import com.lwd.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruiterAdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public PagedResponse<RecruiterResponse> getCompanyRecruiters(
            Long recruiterAdminId,
            int page,
            int size
    ) {

        Company company = companyRepository.findByCreatedById(recruiterAdminId)
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
            Long recruiterAdminId,
            int page,
            int size
    ) {

        Company company = companyRepository.findByCreatedById(recruiterAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<User> recruiterPage = userRepository
                .findByRoleAndCompanyIdAndStatus(
                        Role.RECRUITER,
                        company.getId(),
                        UserStatus.PENDING_APPROVAL,
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
    public RecruiterResponse approveRecruiter(Long recruiterId, Long recruiterAdminId) {

        // 1️⃣ Fetch recruiter
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("User is not a recruiter");
        }
        System.out.println("Approve request");

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

        recruiter.setStatus(block ? UserStatus.SUSPENDED : UserStatus.ACTIVE);
        userRepository.save(recruiter);

        return mapToResponse(recruiter);
    }
    

    public PagedResponse<JobResponse> getJobsByRecruiter(
            Long recruiterId,
            int page,
            int size
    ) {

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("User is not a recruiter");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Job> jobPage = jobRepository
                .findByCreatedById(recruiterId, pageable);

        List<JobResponse> content = jobPage.getContent()
                .stream()
                .map(this::mapToJobResponse)
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
    
    private JobResponse mapToJobResponse(Job job) {

        Company company = job.getCompany();

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .salary(job.getSalary())
                .status(job.getStatus().name())
                .industry(job.getIndustry())
                .createdBy(job.getCreatedBy().getEmail())
                .minExperience(job.getMinExperience())   // new
                .maxExperience(job.getMaxExperience())   // new
                .createdAt(job.getCreatedAt())
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)  // new
                .company(
                        CompanySummaryDTO.builder()
                                .id(company.getId())
                                .companyName(company.getCompanyName())
                                .logo(company.getLogoUrl())
                                .build()
                )
                .build();
    }
}
