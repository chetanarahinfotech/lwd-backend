package com.lwd.jobportal.dto.project;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {

    private Long id;
    private Long userId;

    private String projectTitle;
    private String description;

    private String technologiesUsed;

    private String projectUrl;
    private String githubUrl;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer teamSize;
    private String role;
}
