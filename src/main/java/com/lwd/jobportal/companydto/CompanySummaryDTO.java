package com.lwd.jobportal.companydto;

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
