package com.lwd.jobportal.dto.search;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResponse {

    private List<JobSearchDTO> jobs;

    private List<CompanySearchDTO> companies;

    private List<UserSearchDTO> candidates;

    private List<UserSearchDTO> recruiters;

    private List<SkillDTO> skills;
    
    private int totalPages;

    private long totalElements;
}
