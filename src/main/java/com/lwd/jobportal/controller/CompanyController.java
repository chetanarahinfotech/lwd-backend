package com.lwd.jobportal.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.companydto.CompanyResponse;
import com.lwd.jobportal.dto.companydto.CreateCompanyRequest;
import com.lwd.jobportal.dto.companydto.PagedCompanyResponse;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.CompanyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // ✅ CREATE COMPANY PROFILE
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {

        CompanyResponse response =
                companyService.createCompany(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @GetMapping("/my-company")
    public ResponseEntity<CompanyResponse> getMyCompanyBy() {
    	
    	Long userId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                companyService.getMyCompanyBy(userId)
        );
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<CompanyResponse> getCompanyByCreatedBy(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                companyService.getCompanyByCreatedBy(userId)
        );
    }

    // ✅ UPDATE COMPANY
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CreateCompanyRequest request) {

        CompanyResponse response =
                companyService.updateCompany(id, request);

        return ResponseEntity.ok(response);
    }

    // ✅ DELETE COMPANY (SOFT DELETE)
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(
            @PathVariable Long id) {

        companyService.deleteCompany(id);

        return ResponseEntity.noContent().build();
    }
    
    
    // ✅ GET COMPANY BY ID (Public / Authenticated)
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompany(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                companyService.getCompanyById(id)
        );
    }
    
    // ✅ GET ALL COMPANIES (PAGINATED)
    @GetMapping
    public ResponseEntity<PagedCompanyResponse> getAllCompanies(
            Pageable pageable) {

        return ResponseEntity.ok(
                companyService.getAllCompany(pageable)
        );
    }
    
    // ✅ GET BY INDUSTRY (PAGINATED)
    @GetMapping("/industry")
    public ResponseEntity<PagedCompanyResponse> getCompanyByIndustry(
            @RequestParam String industry,
            Pageable pageable) {

        return ResponseEntity.ok(
                companyService.getCompanyByIndustry(industry, pageable)
        );
    }
    
   
    
}
