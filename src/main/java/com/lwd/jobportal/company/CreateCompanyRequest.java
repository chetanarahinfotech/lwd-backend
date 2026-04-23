package com.lwd.jobportal.company;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    // 🔹 Basic Info
    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String companyName;

    @Size(max = 1000, message = "Description too long")
    private String description;

    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    @Pattern(
        regexp = "^(https?://)?(www\\.)?.+\\..+$",
        message = "Invalid website URL"
    )
    private String website;

    @Size(max = 500, message = "Logo URL too long")
    private String logoUrl;

    // 🔹 Contact Info
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    @Pattern(
        regexp = "^[0-9+\\- ]{7,20}$",
        message = "Invalid phone number"
    )
    private String phone;

    // 🔹 Address Info
    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    // 🔹 Business Info
    @Size(max = 100)
    private String companySize;   // e.g., 1-10, 11-50

    @Pattern(
        regexp = "^[0-9]{4}$",
        message = "Founded year must be 4 digits"
    )
    private String foundedYear;

    @Size(max = 100)
    private String companyType;   // Product / Service / Startup
}