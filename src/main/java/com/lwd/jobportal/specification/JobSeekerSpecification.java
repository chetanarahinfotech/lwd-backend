package com.lwd.jobportal.specification;

import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.entity.Skill;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.NoticeStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobSeekerSpecification {

    private JobSeekerSpecification() {}

    public static Specification<JobSeeker> searchJobSeekers(
            String keyword,
            List<String> skillNames,
            String currentLocation,
            String preferredLocation,
            Integer minExperience,
            Integer maxExperience,
            Double minExpectedCTC,
            Double maxExpectedCTC,
            NoticeStatus noticeStatus,
            Integer maxNoticePeriod,
            Boolean immediateJoiner,
            LocalDate availableBefore
    ) {

        return (root, query, cb) -> {

            boolean isCountQuery = query.getResultType() == Long.class;

            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            boolean hasSkills = skillNames != null && !skillNames.isEmpty();

            // ===============================
            // 🔥 FIX N+1 HERE
            // ===============================
            if (!isCountQuery) {

                // Fetch user always (needed in DTO)
                root.fetch("user", JoinType.LEFT);

                // Fetch skills ONLY when filtering by skill or keyword
                if (hasKeyword || hasSkills) {
                    root.fetch("skills", JoinType.LEFT);
                }

                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            Join<JobSeeker, User> userJoin = root.join("user", JoinType.LEFT);
            Join<JobSeeker, Skill> skillJoin = null;

            if (hasKeyword || hasSkills) {
                skillJoin = root.join("skills", JoinType.LEFT);
            }

            // ===============================
            // KEYWORD SEARCH
            // ===============================
            if (hasKeyword) {

                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                List<Predicate> keywordPredicates = new ArrayList<>();

                keywordPredicates.add(
                        cb.like(cb.lower(userJoin.get("name")), pattern)
                );

                if (skillJoin != null) {
                    keywordPredicates.add(
                            cb.like(cb.lower(skillJoin.get("name")), pattern)
                    );
                }

                keywordPredicates.add(
                        cb.like(cb.lower(root.get("currentCompany")), pattern)
                );

                keywordPredicates.add(
                        cb.like(cb.lower(root.get("currentLocation")), pattern)
                );

                predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
            }

            // ===============================
            // SKILL FILTER
            // ===============================
            if (hasSkills && skillJoin != null) {

                List<String> normalizedSkills = skillNames.stream()
                        .map(s -> s.toLowerCase().trim())
                        .toList();

                predicates.add(
                        cb.lower(skillJoin.get("name")).in(normalizedSkills)
                );
            }

            // ===============================
            // OTHER FILTERS (unchanged)
            // ===============================

            if (currentLocation != null && !currentLocation.trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("currentLocation")),
                                "%" + currentLocation.trim().toLowerCase() + "%")
                );
            }

            if (preferredLocation != null && !preferredLocation.trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("preferredLocation")),
                                "%" + preferredLocation.trim().toLowerCase() + "%")
                );
            }

            if (minExperience != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("totalExperience"), minExperience)
                );
            }

            if (maxExperience != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("totalExperience"), maxExperience)
                );
            }

            if (minExpectedCTC != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("expectedCTC"), minExpectedCTC)
                );
            }

            if (maxExpectedCTC != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("expectedCTC"), maxExpectedCTC)
                );
            }

            if (noticeStatus != null) {
                predicates.add(cb.equal(root.get("noticeStatus"), noticeStatus));
            }

            if (maxNoticePeriod != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("noticePeriod"), maxNoticePeriod)
                );
            }

            if (immediateJoiner != null) {
                predicates.add(
                        cb.equal(root.get("immediateJoiner"), immediateJoiner)
                );
            }

            if (availableBefore != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("availableFrom"), availableBefore)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
