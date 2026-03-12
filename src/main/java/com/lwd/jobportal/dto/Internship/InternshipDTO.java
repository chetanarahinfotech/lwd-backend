package com.lwd.jobportal.dto.Internship;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipDTO {

    private Long id;
    private Long userId;

    private String companyName;
    private String role;

    private LocalDate startDate;
    private LocalDate endDate;

    private String description;
    private String location;

    private String skills;
    private Double stipend;
    private String employmentType;
}
