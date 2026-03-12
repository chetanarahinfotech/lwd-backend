package com.lwd.jobportal.dto.search;

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

    private String location;

    private String industry;
}
