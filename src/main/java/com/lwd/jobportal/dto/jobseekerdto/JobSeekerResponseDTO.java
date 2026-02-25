package com.lwd.jobportal.dto.jobseekerdto;

import com.lwd.jobportal.enums.NoticeStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerResponseDTO {
	
	private Long id;        // Profile ID
	private Long userId;    // Owner user ID


    private String fullName;
    private String email;

    private NoticeStatus noticeStatus;
    private Boolean isServingNotice;
    private LocalDate lastWorkingDay;
    private Integer noticePeriod;
    private LocalDate availableFrom;
    private Boolean immediateJoiner;

    private String currentCompany;
    private Double currentCTC;
    private Double expectedCTC;

    private String currentLocation;
    private String preferredLocation;
    private Integer totalExperience;
    private String resumeUrl;
}
