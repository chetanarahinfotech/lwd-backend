package com.lwd.jobportal.dto.education;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationDTO {

    private Long id;
    private Long userId;

    private String degree;
    private String fieldOfStudy;
    private String institutionName;
    private String university;

    private LocalDate startDate;
    private LocalDate endDate;

    private Double percentage;
    private String grade;
}
