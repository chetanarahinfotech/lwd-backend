package com.lwd.jobportal.company;

import java.time.LocalDateTime;

import com.lwd.jobportal.enums.CompanyStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {

    // 🔹 Basic Info
    private Long id;
    private String companyName;
    private String description;
    private String industry;
    private String website;
    private String logoUrl;

    // 🔹 Contact Info
    private String email;
    private String phone;

    // 🔹 Address Info
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // 🔹 Business Info
    private String companySize;
    private String foundedYear;
    private String companyType;

    // 🔹 Status & Trust
    private CompanyStatus status;
    private Boolean verified;
    private Boolean profileCompleted;

    // 🔹 Ownership
    private Long createdBy;
    private Long approvedBy;
    private LocalDateTime approvedAt;

    // 🔹 Analytics (optional display)
    private Long totalJobsPosted;
    private Long totalHires;
    private Double rating;

    // 🔹 Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}