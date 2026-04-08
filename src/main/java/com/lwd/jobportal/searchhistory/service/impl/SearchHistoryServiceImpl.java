package com.lwd.jobportal.searchhistory.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.searchhistory.dto.RecentSearchResponse;
import com.lwd.jobportal.searchhistory.dto.SaveSearchHistoryRequest;
import com.lwd.jobportal.searchhistory.entity.SearchHistory;
import com.lwd.jobportal.searchhistory.enums.SearchModule;
import com.lwd.jobportal.searchhistory.enums.SearchType;
import com.lwd.jobportal.searchhistory.repository.SearchHistoryRepository;
import com.lwd.jobportal.searchhistory.service.SearchHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveSearchHistory(SaveSearchHistoryRequest request) {
        try {
            if (!isValidRequest(request)) {
                return;
            }
            System.out.println("Call search save mathod");

            String normalizedKeyword = normalizeKeyword(request.getKeyword());
            String normalizedFiltersJson = normalizeFilters(request.getFilters());

            System.out.println("normlised keyword");
            if (isMeaninglessSearch(normalizedKeyword, normalizedFiltersJson)) {
                return;
            }

            String searchHash = generateSearchHash(
                    request.getUserId(),
                    request.getRole(),
                    request.getModule(),
                    request.getSearchType(),
                    normalizedKeyword,
                    normalizedFiltersJson
            );

            LocalDateTime now = LocalDateTime.now();

            Optional<SearchHistory> existingOpt =
                    repository.findTopByUserIdAndSearchHashOrderByLastSeenAtDesc(
                            request.getUserId(),
                            searchHash
                    );

            if (existingOpt.isPresent()) {
                SearchHistory existing = existingOpt.get();

                existing.setKeyword(safeTrim(request.getKeyword(), 255));
                existing.setNormalizedKeyword(safeTrim(normalizedKeyword, 255));
                existing.setFiltersJson(normalizedFiltersJson);
                existing.setLastSeenAt(now);
                existing.setOccurrenceCount(
                        existing.getOccurrenceCount() == null ? 1 : existing.getOccurrenceCount() + 1
                );
                existing.setResultCount(request.getResultCount());
                existing.setSourcePage(safeTrim(request.getSourcePage(), 150));
                existing.setIpAddress(safeTrim(request.getIpAddress(), 64));
                existing.setUserAgent(safeTrim(request.getUserAgent(), 500));

                repository.save(existing);
                System.out.println("Save history");
                return;
            }

            SearchHistory history = SearchHistory.builder()
                    .userId(request.getUserId())
                    .role(request.getRole())
                    .module(request.getModule())
                    .searchType(request.getSearchType())
                    .keyword(safeTrim(request.getKeyword(), 255))
                    .normalizedKeyword(safeTrim(normalizedKeyword, 255))
                    .filtersJson(normalizedFiltersJson)
                    .searchHash(searchHash)
                    .resultCount(request.getResultCount())
                    .sourcePage(safeTrim(request.getSourcePage(), 150))
                    .searchedAt(now)      // first time this unique search was created
                    .lastSeenAt(now)      // latest time this search was used
                    .occurrenceCount(1)
                    .ipAddress(safeTrim(request.getIpAddress(), 64))
                    .userAgent(safeTrim(request.getUserAgent(), 500))
                    .build();

            repository.save(history);

        } catch (Exception e) {
            log.error("Failed to save search history for userId={}",
                    request != null ? request.getUserId() : null, e);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<RecentSearchResponse> getRecentSearches(Long userId, int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 50);

        return repository.findByUserIdAndDeletedFalseOrderByLastSeenAtDescIdDesc(userId)
                .stream()
                .limit(safeLimit)
                .map(this::toRecentSearchResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchHistory> getRecentSearchesByType(Long userId, Role role, SearchType searchType, int limit) {
        int safeLimit = limit <= 0 ? 3 : Math.min(limit, 10);

        return repository.findTop3ByUserIdAndRoleAndSearchTypeOrderByLastSeenAtDescIdDesc(userId, role, searchType)
                .stream()
                .limit(safeLimit)
                .toList();
    }

    @Override
    public void softDeleteById(Long userId, Long id, String deletedBy) {
        SearchHistory history = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Search history not found with id: " + id));

        if (!history.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Search history not found with id: " + id);
        }

        history.setDeleted(true);
        history.setDeletedAt(LocalDateTime.now());
        history.setDeletedBy(deletedBy != null ? deletedBy : "SYSTEM");
        repository.save(history);
    }

    @Override
    public void clearAllForUser(Long userId, String deletedBy) {
        List<SearchHistory> histories = repository.findByUserIdAndDeletedFalseOrderByLastSeenAtDescIdDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        String actor = deletedBy != null ? deletedBy : "SYSTEM";

        for (SearchHistory history : histories) {
            history.setDeleted(true);
            history.setDeletedAt(now);
            history.setDeletedBy(actor);
        }

        repository.saveAll(histories);
    }

    @Override
    public void cleanupOldHistory(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<SearchHistory> oldRecords = repository.findByLastSeenAtBeforeAndDeletedFalse(cutoff);

        if (oldRecords.isEmpty()) {
            return;
        }

        for (SearchHistory history : oldRecords) {
            history.setDeleted(true);
            history.setDeletedAt(LocalDateTime.now());
            history.setDeletedBy("SCHEDULER");
        }

        repository.saveAll(oldRecords);
        log.info("Soft deleted {} old search history records older than {} days", oldRecords.size(), retentionDays);
    }

    private boolean isValidRequest(SaveSearchHistoryRequest request) {
        return request != null
                && request.getUserId() != null
                && request.getRole() != null
                && request.getModule() != null
                && request.getSearchType() != null;
    }

    private boolean isMeaninglessSearch(String keyword, String filtersJson) {
        boolean noKeyword = keyword == null || keyword.isBlank();
        boolean noFilters = filtersJson == null || filtersJson.isBlank() || "{}".equals(filtersJson);
        return noKeyword && noFilters;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String normalized = keyword.trim().replaceAll("\\s+", " ").toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeFilters(Object filters) throws JsonProcessingException {
        if (filters == null) {
            return "{}";
        }

        Object normalizedObject = filters;

		if (filters instanceof String str) {
		    if (str.isBlank()) {
		        return "{}";
		    }
		    return str;
		}

		if (filters instanceof Map<?, ?> map && map.isEmpty()) {
		    return "{}";
		}

		return objectMapper.writeValueAsString(normalizedObject);
    }

    private String generateSearchHash(Long userId,
                                      Role role,
                                      SearchModule module,
                                      SearchType searchType,
                                      String normalizedKeyword,
                                      String normalizedFiltersJson) {
        String raw = userId + "|" +
                role + "|" +
                module + "|" +
                searchType + "|" +
                (normalizedKeyword == null ? "" : normalizedKeyword) + "|" +
                (normalizedFiltersJson == null ? "{}" : normalizedFiltersJson);

        return sha256(raw);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate SHA-256 hash", e);
        }
    }


    private RecentSearchResponse toRecentSearchResponse(SearchHistory history) {
        return RecentSearchResponse.builder()
                .id(history.getId())
                .keyword(history.getKeyword())
                .module(history.getModule())
                .searchType(history.getSearchType())
                .filtersJson(history.getFiltersJson())
                .resultCount(history.getResultCount())
                .occurrenceCount(history.getOccurrenceCount())
                .sourcePage(history.getSourcePage())
                .searchedAt(history.getSearchedAt())
                .lastSeenAt(history.getLastSeenAt())
                .build();
    }

    private String safeTrim(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }

        return trimmed.substring(0, maxLength);
    }
}