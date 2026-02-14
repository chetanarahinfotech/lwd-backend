package com.lwd.jobportal.service;

import java.util.List;

import com.lwd.jobportal.dto.companydto.CompanyResponse;
import com.lwd.jobportal.dto.companydto.CreateCompanyRequest;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long companyId);

    CompanyResponse updateCompany(Long companyId, CreateCompanyRequest request);

    void deleteCompany(Long companyId);
    
    CompanyResponse getMyCompanyBy(Long userId);
    
    CompanyResponse getCompanyByCreatedBy(Long userId);

    List<CompanyResponse> getAllCompany();

    List<CompanyResponse> getCompanyByIndustry();
}
