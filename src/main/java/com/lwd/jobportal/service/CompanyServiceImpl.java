package com.lwd.jobportal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.companydto.CompanyResponse;
import com.lwd.jobportal.companydto.CreateCompanyRequest;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.CompanyRepository;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public CompanyResponse createCompany(CreateCompanyRequest request) {

        if (!SecurityUtils.hasRole(Role.ADMIN)
                && !SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {
            throw new IllegalArgumentException(
                    "Only Admin or Recruiter Admin can create a company"
            );
        }

        if (companyRepository.existsByCompanyName(request.getCompanyName())) {
            throw new IllegalArgumentException("Company already exists");
        }

        Long userId = SecurityUtils.getUserId();

        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .location(request.getLocation())
                .logoUrl(request.getLogoUrl())
                .createdById(userId)   // 🔥 store only ID (better performance)
                .isActive(true)
                .build();

        return mapToResponse(companyRepository.save(company));
    }

    @Override
    public CompanyResponse getCompanyById(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        return mapToResponse(company);
    }

    @Override
    public CompanyResponse updateCompany(Long companyId, CreateCompanyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Long userId = SecurityUtils.getUserId();

        if (SecurityUtils.hasRole(Role.RECRUITER_ADMIN)
                && !company.getCreatedById().equals(userId)) {
            throw new IllegalArgumentException(
                    "You can only update companies you created"
            );
        }

        if (!SecurityUtils.hasRole(Role.ADMIN)
                && !SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {
            throw new IllegalArgumentException(
                    "You do not have permission to update this company"
            );
        }

        if (!company.getCompanyName().equals(request.getCompanyName())
                && companyRepository.existsByCompanyName(request.getCompanyName())) {
            throw new IllegalArgumentException("Company name already exists");
        }

        company.setCompanyName(request.getCompanyName());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setLogoUrl(request.getLogoUrl());

        return mapToResponse(companyRepository.save(company));
    }

    @Override
    public void deleteCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Long userId = SecurityUtils.getUserId();

        if (SecurityUtils.hasRole(Role.ADMIN)) {
            company.setIsActive(false);
            companyRepository.save(company);
            return;
        }

        if (SecurityUtils.hasRole(Role.RECRUITER_ADMIN)) {
            if (!company.getCreatedById().equals(userId)) {
                throw new IllegalArgumentException(
                        "You can only delete companies you created"
                );
            }
            company.setIsActive(false);
            companyRepository.save(company);
            return;
        }

        throw new IllegalArgumentException(
                "You do not have permission to delete this company"
        );
    }

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
                .build();
    }
}
