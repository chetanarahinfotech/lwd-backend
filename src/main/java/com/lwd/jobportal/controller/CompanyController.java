package com.lwd.jobportal.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.dto.companydto.CompanyAnalyticsDTO;
import com.lwd.jobportal.dto.companydto.CompanyResponse;
import com.lwd.jobportal.dto.companydto.CreateCompanyRequest;
import com.lwd.jobportal.dto.companydto.PagedCompanyResponse;
import com.lwd.jobportal.security.SecurityUtils;
import com.lwd.jobportal.service.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company management APIs")
public class CompanyController {

    private final CompanyService companyService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create company profile",
            description = "Create a new company profile. Accessible to ADMIN and RECRUITER_ADMIN only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Company creation request",
                    required = true
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateCompanyRequest request) {

        CompanyResponse response = companyService.createCompany(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my company",
            description = "Fetch company details for the logged-in recruiter, recruiter admin, or admin."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN','RECRUITER')")
    @GetMapping("/my-company")
    public ResponseEntity<CompanyResponse> getMyCompanyBy() {

        Long userId = SecurityUtils.getUserId();

        return ResponseEntity.ok(
                companyService.getMyCompanyBy(userId)
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get company by creator user ID",
            description = "Fetch company details by created-by user ID. Accessible to ADMIN and RECRUITER_ADMIN only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<CompanyResponse> getCompanyByCreatedBy(
            @Parameter(description = "Creator user ID")
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                companyService.getCompanyByCreatedBy(userId)
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update company",
            description = "Update company profile by company ID. Accessible to ADMIN and RECRUITER_ADMIN only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @Parameter(description = "Company ID")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated company request",
                    required = true
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateCompanyRequest request) {

        CompanyResponse response = companyService.updateCompany(id, request);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete company",
            description = "Soft delete company by company ID. Accessible to ADMIN and RECRUITER_ADMIN only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Company deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(
            @Parameter(description = "Company ID")
            @PathVariable Long id) {

        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get company by ID",
            description = "Fetch company details by company ID. Public endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompany(
            @Parameter(description = "Company ID")
            @PathVariable Long id) {

        return ResponseEntity.ok(
                companyService.getCompanyById(id)
        );
    }

    @Operation(
            summary = "Get company analytics",
            description = "Fetch analytics for a company by company ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company analytics fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{id}/analytics")
    public ResponseEntity<CompanyAnalyticsDTO> getAnalytics(
            @Parameter(description = "Company ID")
            @PathVariable Long id) {

        return ResponseEntity.ok(companyService.getAnalytics(id));
    }

    @Operation(
            summary = "Get all companies",
            description = "Fetch paginated list of all companies. Public endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Companies fetched successfully")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number"),
            @Parameter(name = "size", description = "Page size"),
            @Parameter(name = "sort", description = "Sorting criteria, e.g. id,desc")
    })
    @GetMapping
    public ResponseEntity<PagedCompanyResponse> getAllCompanies(Pageable pageable) {

        return ResponseEntity.ok(
                companyService.getAllCompany(pageable)
        );
    }

    @Operation(
            summary = "Search companies",
            description = "Search companies by keyword with pagination. Public endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Companies fetched successfully")
    })
    @GetMapping("/search")
    public PagedCompanyResponse searchCompanies(
            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Page number (default = 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default = 10)")
            @RequestParam(defaultValue = "10") int size
    ) {

        return companyService.searchCompanies(keyword, page, size);
    }

    @Operation(
            summary = "Get companies by industry",
            description = "Fetch paginated companies filtered by industry. Public endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Companies fetched successfully")
    })
    @Parameters({
            @Parameter(name = "industry", description = "Industry name"),
            @Parameter(name = "page", description = "Page number"),
            @Parameter(name = "size", description = "Page size"),
            @Parameter(name = "sort", description = "Sorting criteria")
    })
    @GetMapping("/industry")
    public ResponseEntity<PagedCompanyResponse> getCompanyByIndustry(
            @RequestParam String industry,
            Pageable pageable) {

        return ResponseEntity.ok(
                companyService.getCompanyByIndustry(industry, pageable)
        );
    }
}