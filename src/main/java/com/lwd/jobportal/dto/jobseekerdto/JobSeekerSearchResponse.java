package com.lwd.jobportal.dto.jobseekerdto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerSearchResponse {

    private Long id;
    private Long userId;

    private String fullName;              // from User
    private String email;                 // from User

    private String currentCompany;

    private Integer totalExperience;

    private Double expectedCTC;

    private String currentLocation;

    private Boolean immediateJoiner;

    private Integer noticePeriod;

    private List<String> skills;          // only skill names
}
