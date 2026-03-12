package com.lwd.jobportal.dto.experience;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceDTO {

    private Long id;
    private Long userId;

    private String companyName;
    private String jobTitle;
    private String employmentType;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean currentlyWorking;
    private String jobDescription;
    private String location;
}
