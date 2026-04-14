package com.lwd.jobportal.searchhistory.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.searchhistory.entity.SearchHistory;
import com.lwd.jobportal.searchhistory.enums.SearchType;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Optional<SearchHistory> findTopByUserIdAndSearchHashOrderByLastSeenAtDesc(Long userId, String searchHash);

    List<SearchHistory> findByUserIdOrderByLastSeenAtDescIdDesc(Long userId);

    List<SearchHistory> findTop10ByUserIdOrderByLastSeenAtDescIdDesc(Long userId);

    List<SearchHistory> findTop3ByUserIdAndRoleAndSearchTypeOrderByLastSeenAtDescIdDesc(
            Long userId,
            Role role,
            SearchType searchType
    );

    long countByUserId(Long userId);

    List<SearchHistory> findByUserIdAndDeletedFalseOrderByLastSeenAtDescIdDesc(Long userId);

    List<SearchHistory> findByLastSeenAtBeforeAndDeletedFalse(LocalDateTime cutoff);

    List<SearchHistory> findByUserIdAndRoleAndSearchTypeAndDeletedFalseOrderByLastSeenAtDescIdDesc(
            Long userId,
            Role role,
            SearchType searchType,
            Pageable pageable
    );
}