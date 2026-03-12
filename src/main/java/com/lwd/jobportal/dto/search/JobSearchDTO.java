package com.lwd.jobportal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobSearchDTO {

    private Long id;

    private String title;

    private String location;

    private String industry;

    private String companyName;

    private String jobType;
}
