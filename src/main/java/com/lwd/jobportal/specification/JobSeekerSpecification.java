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

            query.distinct(true); // prevent duplicates (many-to-many)

            List<Predicate> predicates = new ArrayList<>();

            // =========================================
            // JOIN USER (Always needed for name search)
            // =========================================
            Join<JobSeeker, User> userJoin = root.join("user", JoinType.LEFT);

            // =========================================
            // CONDITIONAL SKILL JOIN
            // =========================================
            Join<JobSeeker, Skill> skillJoin = null;

            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            boolean hasSkills = skillNames != null && !skillNames.isEmpty();

            if (hasKeyword || hasSkills) {
                skillJoin = root.join("skills", JoinType.LEFT);
            }

            // =========================================
            // KEYWORD SEARCH (Name + Skill + Company + Location)
            // =========================================
            if (hasKeyword) {

                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                List<Predicate> keywordPredicates = new ArrayList<>();

                // 🔹 Name (User table)
                keywordPredicates.add(
                        cb.like(cb.lower(userJoin.get("name")), pattern)
                );

                // 🔹 Skill name
                if (skillJoin != null) {
                    keywordPredicates.add(
                            cb.like(cb.lower(skillJoin.get("name")), pattern)
                    );
                }

                // 🔹 Company
                keywordPredicates.add(
                        cb.like(cb.lower(root.get("currentCompany")), pattern)
                );

                // 🔹 Current Location
                keywordPredicates.add(
                        cb.like(cb.lower(root.get("currentLocation")), pattern)
                );

                predicates.add(
                        cb.or(keywordPredicates.toArray(new Predicate[0]))
                );
            }

            // =========================================
            // EXACT SKILL FILTER (Multi Skill IN)
            // =========================================
            if (hasSkills && skillJoin != null) {

                List<String> normalizedSkills = skillNames.stream()
                        .map(s -> s.toLowerCase().trim())
                        .toList();

                predicates.add(
                        cb.lower(skillJoin.get("name")).in(normalizedSkills)
                );
            }

            // =========================================
            // LOCATION FILTERS
            // =========================================
            if (currentLocation != null && !currentLocation.trim().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("currentLocation")),
                                "%" + currentLocation.trim().toLowerCase() + "%"
                        )
                );
            }

            if (preferredLocation != null && !preferredLocation.trim().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("preferredLocation")),
                                "%" + preferredLocation.trim().toLowerCase() + "%"
                        )
                );
            }

            // =========================================
            // EXPERIENCE FILTER
            // =========================================
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

            // =========================================
            // EXPECTED CTC FILTER
            // =========================================
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

            // =========================================
            // NOTICE FILTERS
            // =========================================
            if (noticeStatus != null) {
                predicates.add(
                        cb.equal(root.get("noticeStatus"), noticeStatus)
                );
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
