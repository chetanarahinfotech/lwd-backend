package com.lwd.jobportal.companydto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String description;
    private String website;
    private String location;
    private String logoUrl;
}
