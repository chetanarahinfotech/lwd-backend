package com.lwd.jobportal.dto.jobseekerdto;

import com.lwd.jobportal.enums.NoticeStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerSearchRequest {

    // 🔎 Keyword search
    private String keyword;

    // 🛠 Skill filters
    private List<String> skills;

    // 📍 Location filters
    private String currentLocation;
    private String preferredLocation;

    // 💼 Experience range
    private Integer minExperience;
    private Integer maxExperience;

    // 💰 Expected CTC range
    private Double minExpectedCTC;
    private Double maxExpectedCTC;

    // 📄 Notice filters
    private NoticeStatus noticeStatus;
    private Integer maxNoticePeriod;
    private Boolean immediateJoiner;

    // 📅 Availability
    private LocalDate availableBefore;

    // 📄 Pagination
    private Integer page = 0;
    private Integer size = 10;

    // 🔽 Sorting
    private String sortBy = "totalExperience";
    private String sortDirection = "DESC";
}
