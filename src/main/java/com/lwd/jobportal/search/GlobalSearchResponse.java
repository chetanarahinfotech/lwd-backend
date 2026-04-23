package com.lwd.jobportal.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
