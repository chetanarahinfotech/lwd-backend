package com.lwd.jobportal.searchhistory.service;

import java.util.List;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.searchhistory.dto.RecentSearchResponse;
import com.lwd.jobportal.searchhistory.dto.SaveSearchHistoryRequest;
import com.lwd.jobportal.searchhistory.entity.SearchHistory;
import com.lwd.jobportal.searchhistory.enums.SearchType;

public interface SearchHistoryService {

    void saveSearchHistory(SaveSearchHistoryRequest request);

    List<RecentSearchResponse> getRecentSearches(Long userId, int limit);

    List<SearchHistory> getRecentSearchesByType(Long userId, Role role, SearchType searchType, int limit);

    void softDeleteById(Long userId, Long id, String deletedBy);

    void clearAllForUser(Long userId, String deletedBy);

    void cleanupOldHistory(int retentionDays);
}