package com.lwd.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lwd.jobportal.dto.search.GlobalSuggestionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lwd.jobportal.entity.Job;

public interface GlobalSearchRepository extends JpaRepository<Job, Long> {

    @Query(value = """

        SELECT j.id AS id, j.title AS label, 'JOB' AS type
        FROM jobs j
        WHERE j.deleted = false
        AND LOWER(j.title) LIKE LOWER(CONCAT(:keyword, '%'))

        UNION

        SELECT c.id AS id, c.company_name AS label, 'COMPANY' AS type
        FROM companies c
        WHERE c.is_active = true
        AND LOWER(c.company_name) LIKE LOWER(CONCAT(:keyword, '%'))

        UNION

        SELECT s.id AS id, s.name AS label, 'SKILL' AS type
        FROM skills s
        WHERE LOWER(s.name) LIKE LOWER(CONCAT(:keyword, '%'))

        UNION

        SELECT u.id AS id, u.name AS label, 'CANDIDATE' AS type
        FROM users u
        WHERE u.role = 'JOB_SEEKER'
        AND LOWER(u.name) LIKE LOWER(CONCAT(:keyword, '%'))

        UNION

        SELECT u.id AS id, u.name AS label, 'RECRUITER' AS type
        FROM users u
        WHERE u.role = 'RECRUITER'
        AND LOWER(u.name) LIKE LOWER(CONCAT(:keyword, '%'))

        ORDER BY label
        LIMIT 10

        """, nativeQuery = true)
    List<GlobalSuggestionProjection> searchSuggestions(
            @Param("keyword") String keyword
    );
}
