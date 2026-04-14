package com.lwd.jobportal.searchhistory.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.searchhistory.dto.RecentSearchResponse;
import com.lwd.jobportal.searchhistory.service.SearchHistoryService;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    /**
     * Replace this with your actual SecurityUtils.getUserId()
     */
    private Long getCurrentUserId() {
    	Long userId = SecurityUtils.getUserId();
        return userId;
    }

    /**
     * Replace this with your actual current email / username fetch
     */
    private String getCurrentActor() {
        return "SYSTEM";
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('JOB_SEEKER','RECRUITER','RECRUITER_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<RecentSearchResponse>> getRecentSearches(
            @RequestParam(defaultValue = "5") int limit) {

        Long userId = getCurrentUserId();
        return ResponseEntity.ok(searchHistoryService.getRecentSearches(userId, limit));
    }
    
    @GetMapping("/candidates/recent")
    @PreAuthorize("hasAnyRole('RECRUITER','RECRUITER_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecentCandidateSearches(
            @RequestParam(defaultValue = "3") int limit
    ) {
        Long userId = SecurityUtils.getUserId();
        Role role = SecurityUtils.getRole();

        List<RecentSearchResponse> data =
                searchHistoryService.getRecentCandidateSearches(userId, role, limit);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recent candidate searches fetched successfully",
                "data", data
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('JOB_SEEKER','RECRUITER','RECRUITER_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSearchHistory(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        String actor = getCurrentActor();

        searchHistoryService.softDeleteById(userId, id, actor);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Search history deleted successfully"
        ));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasAnyRole('JOB_SEEKER','RECRUITER','RECRUITER_ADMIN','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> clearAllSearchHistory() {
        Long userId = getCurrentUserId();
        String actor = getCurrentActor();

        searchHistoryService.clearAllForUser(userId, actor);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All search history cleared successfully"
        ));
    }
}