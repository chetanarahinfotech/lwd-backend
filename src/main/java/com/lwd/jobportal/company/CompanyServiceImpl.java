package com.lwd.jobportal.company;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.CompanyStatus;
import com.lwd.jobportal.enums.Role;
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
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    public CompanyResponse createCompany(CreateCompanyRequest request) {

        if (!SecurityUtils.hasRole(Role.ADMIN) &&
            !SecurityUtils.hasRole(Role.COMPANY_ADMIN)) {
            throw new ForbiddenActionException(
                    "Only ADMIN or COMPANY_ADMIN can create a company"
            );
        }

        String companyName = normalize(request.getCompanyName());
        if (companyName == null) {
            throw new InvalidOperationException("Company name is required");
        }

        if (companyRepository.existsByCompanyName(companyName)) {
            throw new InvalidOperationException("Company already exists");
        }

        String email = normalize(request.getEmail());
        if (email != null && companyRepository.existsByEmail(email)) {
            throw new InvalidOperationException("Company email already exists");
        }

        Long userId = SecurityUtils.getUserId();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        Company company = Company.builder()
                .companyName(companyName)
                .description(normalize(request.getDescription()))
                .industry(normalize(request.getIndustry()))
                .website(normalize(request.getWebsite()))
                .logoUrl(normalize(request.getLogoUrl()))
                .email(email)
                .phone(normalize(request.getPhone()))
                .address(normalize(request.getAddress()))
                .city(normalize(request.getCity()))
                .state(normalize(request.getState()))
                .country(normalize(request.getCountry()))
                .postalCode(normalize(request.getPostalCode()))
                .companySize(normalize(request.getCompanySize()))
                .foundedYear(normalize(request.getFoundedYear()))
                .companyType(normalize(request.getCompanyType()))
                .status(isAdmin ? CompanyStatus.ACTIVE : CompanyStatus.PENDING)
                .verified(isAdmin)
                .profileCompleted(false)
                .createdBy(currentUser)
                .approvedBy(isAdmin ? currentUser : null)
                .approvedAt(isAdmin ? LocalDateTime.now() : null)
                .totalJobsPosted(0L)
                .totalHires(0L)
                .rating(null)
                .build();

        updateProfileCompleted(company);

        Company savedCompany = companyRepository.save(company);

        if (currentUser.getRole() == Role.COMPANY_ADMIN) {
            currentUser.setCompany(savedCompany);
            userRepository.save(currentUser);
        }

        return mapToResponse(savedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getMyCompanyBy(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() != null) {
                return mapToResponse(user.getCompany());
            }

            return companyRepository.findByCreatedBy_Id(userId)
                    .map(this::mapToResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        }

        if (user.getRole() == Role.RECRUITER) {
            if (user.getCompany() == null) {
                throw new ResourceNotFoundException("Recruiter is not assigned to any company");
            }
            return mapToResponse(user.getCompany());
        }

        throw new ForbiddenActionException("Access Denied");
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyByCreatedBy(Long userId) {

        if (!SecurityUtils.hasRole(Role.ADMIN) &&
            !SecurityUtils.hasRole(Role.COMPANY_ADMIN)) {
            throw new ForbiddenActionException("Access Denied");
        }

        return companyRepository.findByCreatedBy_Id(userId)
                .map(this::mapToResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found for user id: " + userId)
                );
    }

    @Override
    public CompanyResponse updateCompany(Long companyId, CreateCompanyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Long userId = SecurityUtils.getUserId();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!SecurityUtils.hasRole(Role.ADMIN) &&
            !SecurityUtils.hasRole(Role.COMPANY_ADMIN)) {
            throw new ForbiddenActionException(
                    "You do not have permission to update this company"
            );
        }

        if (SecurityUtils.hasRole(Role.COMPANY_ADMIN)) {
            if (currentUser.getCompany() == null ||
                !company.getId().equals(currentUser.getCompany().getId())) {
                throw new ForbiddenActionException(
                        "You can only update your own company"
                );
            }
        }

        String companyName = normalize(request.getCompanyName());
        if (companyName == null) {
            throw new InvalidOperationException("Company name is required");
        }

        if (!company.getCompanyName().equalsIgnoreCase(companyName)
                && companyRepository.existsByCompanyName(companyName)) {
            throw new InvalidOperationException("Company name already exists");
        }

        String email = normalize(request.getEmail());
        if (email != null &&
            company.getEmail() != null &&
            !company.getEmail().equalsIgnoreCase(email) &&
            companyRepository.existsByEmail(email)) {
            throw new InvalidOperationException("Company email already exists");
        }

        if (email != null && company.getEmail() == null && companyRepository.existsByEmail(email)) {
            throw new InvalidOperationException("Company email already exists");
        }

        company.setCompanyName(companyName);
        company.setDescription(normalize(request.getDescription()));
        company.setIndustry(normalize(request.getIndustry()));
        company.setWebsite(normalize(request.getWebsite()));
        company.setLogoUrl(normalize(request.getLogoUrl()));
        company.setEmail(email);
        company.setPhone(normalize(request.getPhone()));
        company.setAddress(normalize(request.getAddress()));
        company.setCity(normalize(request.getCity()));
        company.setState(normalize(request.getState()));
        company.setCountry(normalize(request.getCountry()));
        company.setPostalCode(normalize(request.getPostalCode()));
        company.setCompanySize(normalize(request.getCompanySize()));
        company.setFoundedYear(normalize(request.getFoundedYear()));
        company.setCompanyType(normalize(request.getCompanyType()));

        updateProfileCompleted(company);

        return mapToResponse(companyRepository.save(company));
    }

    @Override
    public void deleteCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Long userId = SecurityUtils.getUserId();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (SecurityUtils.hasRole(Role.ADMIN)) {
            company.setStatus(CompanyStatus.SUSPENDED);
            companyRepository.save(company);
            return;
        }

        if (SecurityUtils.hasRole(Role.COMPANY_ADMIN)) {
            if (currentUser.getCompany() == null ||
                !company.getId().equals(currentUser.getCompany().getId())) {
                throw new ForbiddenActionException(
                        "You can only delete your own company"
                );
            }

            company.setStatus(CompanyStatus.SUSPENDED);
            companyRepository.save(company);
            return;
        }

        throw new ForbiddenActionException(
                "You do not have permission to delete this company"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        return mapToResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyAnalyticsDTO getAnalytics(Long companyId) {
        return companyRepository.getCompanyAnalytics(companyId);
    }

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    public PagedCompanyResponse getCompanyByIndustry(String industry, Pageable pageable) {

        Page<Company> companyPage = companyRepository.findByIndustry(industry, pageable);

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
    @Transactional(readOnly = true)
    public PagedCompanyResponse searchCompanies(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Company> companyPage = companyRepository.searchCompanies(keyword, pageable);

        List<CompanyResponse> companies = companyPage.getContent()
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

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .description(company.getDescription())
                .industry(company.getIndustry())
                .website(company.getWebsite())
                .logoUrl(company.getLogoUrl())
                .email(company.getEmail())
                .phone(company.getPhone())
                .address(company.getAddress())
                .city(company.getCity())
                .state(company.getState())
                .country(company.getCountry())
                .postalCode(company.getPostalCode())
                .companySize(company.getCompanySize())
                .foundedYear(company.getFoundedYear())
                .companyType(company.getCompanyType())
                .status(company.getStatus())
                .verified(company.getVerified())
                .profileCompleted(company.getProfileCompleted())
                .createdBy(company.getCreatedBy() != null ? company.getCreatedBy().getId() : null)
                .approvedBy(company.getApprovedBy() != null ? company.getApprovedBy().getId() : null)
                .approvedAt(company.getApprovedAt())
                .totalJobsPosted(company.getTotalJobsPosted())
                .totalHires(company.getTotalHires())
                .rating(company.getRating())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private void updateProfileCompleted(Company company) {
        boolean completed =
                company.getCompanyName() != null &&
                company.getDescription() != null &&
                company.getIndustry() != null &&
                company.getWebsite() != null &&
                company.getLogoUrl() != null &&
                company.getEmail() != null &&
                company.getPhone() != null &&
                company.getAddress() != null &&
                company.getCity() != null &&
                company.getState() != null &&
                company.getCountry() != null &&
                company.getPostalCode() != null &&
                company.getCompanySize() != null &&
                company.getFoundedYear() != null &&
                company.getCompanyType() != null;

        company.setProfileCompleted(completed);
    }
}