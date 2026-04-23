package com.lwd.jobportal.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanySummaryDTO {

    private Long id;
    private String companyName;
    private String logo;
}
