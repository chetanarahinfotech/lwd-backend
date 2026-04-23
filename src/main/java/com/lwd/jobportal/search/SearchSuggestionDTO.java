package com.lwd.jobportal.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchSuggestionDTO {

    private Long id;
    private String label;
    private String type; // JOB, COMPANY, SKILL, CANDIDATE, RECRUITER
}
