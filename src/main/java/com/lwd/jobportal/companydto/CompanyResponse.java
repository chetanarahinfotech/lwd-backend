package com.lwd.jobportal.companydto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {

    private Long id;
    private String companyName;
    private String description;
    private String website;
    private String location;
    private String logoUrl;
    private Boolean isActive;
    private Long createdBy; // recruiter email or name
}
