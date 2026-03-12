package com.lwd.jobportal.service;

import org.springframework.data.domain.Pageable;

import com.lwd.jobportal.dto.companydto.CompanyAnalyticsDTO;
import com.lwd.jobportal.dto.companydto.CompanyResponse;
import com.lwd.jobportal.dto.companydto.CreateCompanyRequest;
import com.lwd.jobportal.dto.companydto.PagedCompanyResponse;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long companyId);
    
    CompanyAnalyticsDTO getAnalytics(Long companyId);

    CompanyResponse updateCompany(Long companyId, CreateCompanyRequest request);

    void deleteCompany(Long companyId);

    CompanyResponse getMyCompanyBy(Long userId);

    CompanyResponse getCompanyByCreatedBy(Long userId);

    PagedCompanyResponse getAllCompany(Pageable pageable);

    // ✅ Updated with Pageable + industry parameter
    PagedCompanyResponse getCompanyByIndustry(String industry, Pageable pageable);

	PagedCompanyResponse searchCompanies(String keyword, int page, int size);
}
