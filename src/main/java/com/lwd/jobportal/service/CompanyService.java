package com.lwd.jobportal.service;

import com.lwd.jobportal.companydto.CreateCompanyRequest;
import com.lwd.jobportal.companydto.CompanyResponse;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long companyId);

    CompanyResponse updateCompany(Long companyId, CreateCompanyRequest request);

    void deleteCompany(Long companyId);
}
