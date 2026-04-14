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
import java.util.stream.Collectors;

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
            boolean hasKeyword = keyword != null && !keyword.isBlank();
            boolean hasSkills = skillNames != null && !skillNames.isEmpty();

            List<Predicate> predicates = new ArrayList<>();

            query.distinct(true);

            Join<JobSeeker, User> userJoin = root.join("user", JoinType.LEFT);
            Join<JobSeeker, Skill> skillJoin = null;

            // Fetch joins only when safe
            if (!isCountQuery && !hasSkills) {
                root.fetch("user", JoinType.LEFT);
                root.fetch("skills", JoinType.LEFT);
            }

            if (hasSkills) {
                skillJoin = root.join("skills", JoinType.LEFT);
            }

            if (hasKeyword) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(userJoin.get("name")), pattern),
                                cb.like(cb.lower(root.get("currentCompany")), pattern),
                                cb.like(cb.lower(root.get("currentLocation")), pattern),
                                cb.like(cb.lower(root.get("headline")), pattern)
                        )
                );
            }

            if (hasSkills && skillJoin != null) {
                List<String> normalizedSkills = skillNames.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());

                predicates.add(cb.lower(skillJoin.get("name")).in(normalizedSkills));
            }

            if (currentLocation != null && !currentLocation.isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("currentLocation")),
                                "%" + currentLocation.toLowerCase() + "%")
                );
            }

            if (preferredLocation != null && !preferredLocation.isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("preferredLocation")),
                                "%" + preferredLocation.toLowerCase() + "%")
                );
            }

            if (minExperience != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalExperience"), minExperience));
            }

            if (maxExperience != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalExperience"), maxExperience));
            }

            if (minExpectedCTC != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expectedCTC"), minExpectedCTC));
            }

            if (maxExpectedCTC != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expectedCTC"), maxExpectedCTC));
            }

            if (noticeStatus != null) {
                predicates.add(cb.equal(root.get("noticeStatus"), noticeStatus));
            }

            if (maxNoticePeriod != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("noticePeriod"), maxNoticePeriod));
            }

            if (immediateJoiner != null) {
                predicates.add(cb.equal(root.get("immediateJoiner"), immediateJoiner));
            }

            if (availableBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("availableFrom"), availableBefore));
            }

            if (!isCountQuery && hasSkills && skillJoin != null) {
                query.groupBy(root.get("id"));

                Expression<Long> skillMatchCount = cb.countDistinct(skillJoin.get("id"));

                query.orderBy(
                        cb.desc(skillMatchCount),
                        cb.desc(root.get("profileCompletion")),
                        cb.desc(root.get("totalExperience"))
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}