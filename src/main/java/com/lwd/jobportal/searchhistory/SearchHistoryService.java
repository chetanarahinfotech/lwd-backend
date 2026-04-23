package com.lwd.jobportal.searchhistory;

import java.util.List;

import com.lwd.jobportal.enums.Role;

public interface SearchHistoryService {

    void saveSearchHistory(SaveSearchHistoryRequest request);

    List<RecentSearchResponse> getRecentSearches(Long userId, int limit);

    List<SearchHistory> getRecentSearchesByType(Long userId, Role role, SearchType searchType, int limit);

    void softDeleteById(Long userId, Long id, String deletedBy);

    void clearAllForUser(Long userId, String deletedBy);

    void cleanupOldHistory(int retentionDays);

	List<RecentSearchResponse> getRecentCandidateSearches(Long userId, Role role, int limit);
}