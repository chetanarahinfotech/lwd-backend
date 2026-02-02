package com.lwd.jobportal.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.companydto.CompanyResponse;
import com.lwd.jobportal.companydto.CreateCompanyRequest;
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

    // ✅ GET COMPANY BY ID (Public / Authenticated)
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompany(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                companyService.getCompanyById(id)
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
}
