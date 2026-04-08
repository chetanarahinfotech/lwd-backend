package com.lwd.jobportal.searchhistory.dto;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.searchhistory.enums.SearchModule;
import com.lwd.jobportal.searchhistory.enums.SearchType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveSearchHistoryRequest {

    private Long userId;
    private Role role;
    private SearchModule module;
    private SearchType searchType;

    private String keyword;

    /**
     * Can be request DTO object or Map<String, Object>
     */
    private Object filters;

    private Integer resultCount;
    private String sourcePage;
    private String ipAddress;
    private String userAgent;
}