package com.lwd.jobportal.dto.companydto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanySummaryDTO {

    private Long id;
    private String companyName;
    private String logo;
}
