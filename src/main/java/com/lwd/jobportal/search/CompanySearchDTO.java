package com.lwd.jobportal.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySearchDTO {

    private Long id;

    private String companyName;

    private String city;

    private String industry;
}
