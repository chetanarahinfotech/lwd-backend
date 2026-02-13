package com.lwd.jobportal.service;

import com.lwd.jobportal.companydto.CreateCompanyRequest;

import java.util.List;

import com.lwd.jobportal.companydto.CompanyResponse;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long companyId);

    CompanyResponse updateCompany(Long companyId, CreateCompanyRequest request);

    void deleteCompany(Long companyId);
    
    CompanyResponse getMyCompanyBy(Long userId);
    
    CompanyResponse getCompanyByCreatedBy(Long userId);

    List<CompanyResponse> getAllCompany();
}
