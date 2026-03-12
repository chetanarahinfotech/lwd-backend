package com.lwd.jobportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.dto.companydto.CompanyAnalyticsDTO;
import com.lwd.jobportal.dto.companydto.CompanyResponse;
import com.lwd.jobportal.dto.companydto.CreateCompanyRequest;
import com.lwd.jobportal.dto.companydto.PagedCompanyResponse;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ForbiddenActionException;
import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * ============================================================
 * CompanyServiceImpl
 * ============================================================
 *
 * Handles all business logic related to Company management.
 *
 * Responsibilities:
 *  - Create company
 *  - Update company
 *  - Soft delete company
 *  - Fetch company details
 *  - Industry-based filtering
 *
 * Security Model:
 *  - ADMIN: Full access
 *  - RECRUITER_ADMIN: Can manage only companies they created
 *  - RECRUITER: Can view assigned company
 *
 * All operations are transactional.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    // Repository for Company persistence operations
    private final CompanyRepository companyRepository;

    // Repository for User operations (used for recruiter-company mapping)
    private final UserRepository userRepository;

    // ============================================================
    // ======================= CREATE COMPANY =====================
    // ============================================================

    /**
     * Creates a new company.
     *
     * Only ADMIN or RECRUITER_ADMIN can create companies.
     */
    @Override
    public CompanyResponse createCompany(CreateCompanyRequest request) {

        // Role validation
        if (!SecurityUtils.hasRole(Role.ADMIN)
                && !SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {
            throw new ForbiddenActionException(
                    "Only ADMIN or RECRUITER_ADMIN can create a company"
            );
        }

        // Check duplicate company name
        if (companyRepository.existsByCompanyName(request.getCompanyName())) {
            throw new InvalidOperationException("Company already exists");
        }

        // Get currently logged-in user ID
        Long userId = SecurityUtils.getUserId();

        // Build company entity
        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .location(request.getLocation())
                .logoUrl(request.getLogoUrl())
                .createdById(userId)
                .isActive(true) // Default active on creation
                .build();

        return mapToResponse(companyRepository.save(company));
    }

    // ============================================================
    // ===================== GET MY COMPANY =======================
    // ============================================================

    /**
     * Fetch company associated with a given user.
     *
     * ADMIN / RECRUITER_ADMIN:
     *   - Fetch by createdById
     *
     * RECRUITER:
     *   - Fetch assigned company
     */
    @Override
    public CompanyResponse getMyCompanyBy(Long userId) {

        // ADMIN / RECRUITER_ADMIN logic
        if (SecurityUtils.hasRole(Role.ADMIN) ||
            SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {

            return companyRepository.findByCreatedById(userId)
                    .map(this::mapToResponse)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Company not found"));
        }

        // RECRUITER logic
        if (SecurityUtils.hasRole(Role.RECRUITER)) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found"));

            Company company = user.getCompany();

            if (company == null) {
                throw new ResourceNotFoundException(
                        "Recruiter is not assigned to any company");
            }

            return mapToResponse(company);
        }

        // Other roles not allowed
        throw new ForbiddenActionException("Access Denied");
    }

    // ============================================================
    // ================= GET COMPANY BY CREATOR ===================
    // ============================================================

    /**
     * Fetch company created by a specific user.
     * Only ADMIN or RECRUITER_ADMIN allowed.
     */
    @Override
    public CompanyResponse getCompanyByCreatedBy(Long userId) {

        if (!SecurityUtils.hasRole(Role.ADMIN) &&
            !SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {

            throw new ForbiddenActionException("Access Denied");
        }

        return companyRepository.findByCreatedById(userId)
                .map(this::mapToResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found for user id: " + userId
                        )
                );
    }

    // ============================================================
    // ======================= UPDATE COMPANY =====================
    // ============================================================

    /**
     * Updates an existing company.
     *
     * ADMIN:
     *  - Can update any company.
     *
     * RECRUITER_ADMIN:
     *  - Can update only companies they created.
     */
    @Override
    public CompanyResponse updateCompany(Long companyId, CreateCompanyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Long userId = SecurityUtils.getUserId();

        // Role validation
        if (!SecurityUtils.hasRole(Role.ADMIN)
                && !SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {
            throw new ForbiddenActionException(
                    "You do not have permission to update this company"
            );
        }

        // Recruiter Admin ownership validation
        if (SecurityUtils.hasRole(Role.RECRUITER_ADMIN)
                && !company.getCreatedById().equals(userId)) {
            throw new ForbiddenActionException(
                    "You can only update companies you created"
            );
        }

        // Prevent duplicate company names
        if (!company.getCompanyName().equals(request.getCompanyName())
                && companyRepository.existsByCompanyName(request.getCompanyName())) {
            throw new InvalidOperationException("Company name already exists");
        }

        // Update fields
        company.setCompanyName(request.getCompanyName());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setLogoUrl(request.getLogoUrl());

        return mapToResponse(companyRepository.save(company));
    }

    // ============================================================
    // ======================= DELETE COMPANY =====================
    // ============================================================

    /**
     * Soft delete company (sets isActive = false).
     *
     * ADMIN:
     *   - Can delete any company
     *
     * RECRUITER_ADMIN:
     *   - Can delete only companies they created
     */
    @Override
    public void deleteCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Long userId = SecurityUtils.getUserId();

        // ADMIN: Full access
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            company.setIsActive(false);
            companyRepository.save(company);
            return;
        }

        // RECRUITER_ADMIN: Ownership check
        if (SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {
            if (!company.getCreatedById().equals(userId)) {
                throw new ForbiddenActionException(
                        "You can only delete companies you created"
                );
            }
            company.setIsActive(false);
            companyRepository.save(company);
            return;
        }

        // Other roles not allowed
        throw new ForbiddenActionException(
                "You do not have permission to delete this company"
        );
    }

    // ============================================================
    // ======================= GET COMPANY ========================
    // ============================================================

    /**
     * Fetch company by ID.
     */
    @Override
    public CompanyResponse getCompanyById(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        return mapToResponse(company);
    }
    
    
    public CompanyAnalyticsDTO getAnalytics(Long companyId) {
        return companyRepository.getCompanyAnalytics(companyId);
    }


    // ============================================================
    // ======================= GET ALL COMPANIES ==================
    // ============================================================

    /**
     * Fetch all companies with pagination.
     */
    @Override
    public PagedCompanyResponse getAllCompany(Pageable pageable) {

        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<CompanyResponse> content = companyPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedCompanyResponse(
                content,
                companyPage.getNumber(),
                companyPage.getSize(),
                companyPage.getTotalElements(),
                companyPage.getTotalPages(),
                companyPage.isLast()
        );
    }

    // ============================================================
    // ================== GET COMPANY BY INDUSTRY =================
    // ============================================================

    /**
     * Fetch companies filtered by industry.
     */
    @Override
    public PagedCompanyResponse getCompanyByIndustry(String industry, Pageable pageable) {

        Page<Company> companyPage =
                companyRepository.findByIndustry(industry, pageable);

        List<CompanyResponse> content = companyPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedCompanyResponse(
                content,
                companyPage.getNumber(),
                companyPage.getSize(),
                companyPage.getTotalElements(),
                companyPage.getTotalPages(),
                companyPage.isLast()
        );
    }
    
    @Override
    public PagedCompanyResponse searchCompanies(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Company> companyPage =
                companyRepository.searchCompanies(keyword, pageable);

        List<CompanyResponse> companies =
                companyPage.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());

        return new PagedCompanyResponse(
                companies,
                companyPage.getNumber(),
                companyPage.getSize(),
                companyPage.getTotalElements(),
                companyPage.getTotalPages(),
                companyPage.isLast()
        );
    }

    // ============================================================
    // ======================= MAPPER =============================
    // ============================================================

    /**
     * Converts Company entity to CompanyResponse DTO.
     */
    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .location(company.getLocation())
                .logoUrl(company.getLogoUrl())
                .isActive(company.getIsActive())
                .createdBy(company.getCreatedById())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
