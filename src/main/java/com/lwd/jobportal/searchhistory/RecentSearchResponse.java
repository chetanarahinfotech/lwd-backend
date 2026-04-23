package com.lwd.jobportal.searchhistory;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentSearchResponse {

    private Long id;
    private String keyword;
    private SearchModule module;
    private SearchType searchType;
    private String filtersJson;
    private Integer resultCount;
    private Integer occurrenceCount;
    private String sourcePage;
    private LocalDateTime searchedAt;
    private LocalDateTime lastSeenAt;
}