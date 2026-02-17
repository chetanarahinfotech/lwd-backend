package com.lwd.jobportal.specification;

import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> searchJobs(
            String keyword,
            String location,
            String industry,
            String companyName,
            Integer minExp,
            Integer maxExp,
            JobType jobType,
            
            // ===== LWD FILTERS =====
            NoticeStatus noticePreference,
            Integer maxNoticePeriod,
            Boolean lwdPreferred,
            
            JobStatus status,
            boolean isPublicRequest   // 🔥 Important
    ) {

        return (root, query, cb) -> {

            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            // =========================================
            // 🔒 GLOBAL FILTERS (Very Important)
            // =========================================

            // Always hide soft-deleted jobs
            predicates.add(cb.isFalse(root.get("deleted")));

            // Public users should only see OPEN jobs
            if (isPublicRequest) {
                predicates.add(cb.equal(root.get("status"), JobStatus.OPEN));
            } else {
                // Admin can filter by status
                if (status != null) {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }

            // =========================================
            // JOIN (ONLY ONCE)
            // =========================================

            Join<Object, Object> companyJoin =
                    root.join("company", JoinType.LEFT);

            // =========================================
            // KEYWORD SEARCH (OR block)
            // =========================================

            if (keyword != null && !keyword.trim().isEmpty()) {

                String pattern = "%" + keyword.trim().toLowerCase() + "%";

                Predicate titleMatch =
                        cb.like(cb.lower(root.get("title")), pattern);

                Predicate locationMatch =
                        cb.like(cb.lower(root.get("location")), pattern);

                Predicate industryMatch =
                        cb.like(cb.lower(root.get("industry")), pattern);

                Predicate companyMatch =
                        cb.like(cb.lower(companyJoin.get("companyName")), pattern);

                Predicate jobTypeMatch =
                        cb.like(cb.lower(root.get("jobType").as(String.class)), pattern);

                predicates.add(
                        cb.or(titleMatch, locationMatch, industryMatch, companyMatch, jobTypeMatch)
                );
            }

            // =========================================
            // DIRECT FILTERS (AND block)
            // =========================================

            if (location != null && !location.trim().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("location")),
                                "%" + location.trim().toLowerCase() + "%"
                        )
                );
            }

            if (industry != null && !industry.trim().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("industry")),
                                "%" + industry.trim().toLowerCase() + "%"
                        )
                );
            }

            if (companyName != null && !companyName.trim().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(companyJoin.get("companyName")),
                                "%" + companyName.trim().toLowerCase() + "%"
                        )
                );
            }

            if (minExp != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("minExperience"),
                                minExp
                        )
                );
            }

            if (maxExp != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("maxExperience"),
                                maxExp
                        )
                );
            }

            if (jobType != null) {
                predicates.add(
                        cb.equal(root.get("jobType"), jobType)
                );
            }
            
         // =========================================
         // LWD FILTERS
         // =========================================

         if (noticePreference != null) {
             predicates.add(
                     cb.equal(root.get("noticePreference"), noticePreference)
             );
         }

         if (maxNoticePeriod != null) {
             predicates.add(
                     cb.or(
                             cb.isNull(root.get("maxNoticePeriod")),
                             cb.greaterThanOrEqualTo(
                                     root.get("maxNoticePeriod"),
                                     maxNoticePeriod
                             )
                     )
             );
         }

         if (lwdPreferred != null) {
             predicates.add(
                     cb.equal(root.get("lwdPreferred"), lwdPreferred)
             );
         }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<Job> publicJobs() {
        return (root, query, cb) -> {

            if (query.getResultType() != Long.class) {
                root.fetch("company", JoinType.LEFT);
                root.fetch("createdBy", JoinType.LEFT);
                query.distinct(true);
            }

            return cb.and(
                    cb.isFalse(root.get("deleted")),
                    cb.equal(root.get("status"), JobStatus.OPEN)
            );
        };
    }
    
    public static Specification<Job> similarJobs(
            String industry,
            JobType jobType,
            Long jobId
    ) {
        return (root, query, cb) -> {

            if (query.getResultType() != Long.class) {

                // Fetch Job → Company
                root.fetch("company", JoinType.LEFT);

                // Fetch Job → CreatedBy
                Fetch<Job, User> userFetch =
                        root.fetch("createdBy", JoinType.LEFT);

                // Fetch CreatedBy → Company (if needed)
                userFetch.fetch("company", JoinType.LEFT);

                query.distinct(true);
            }

            return cb.and(
                    cb.equal(root.get("status"), JobStatus.OPEN),
                    cb.equal(root.get("industry"), industry),
                    cb.equal(root.get("jobType"), jobType),
                    cb.notEqual(root.get("id"), jobId),
                    cb.isFalse(root.get("deleted"))
            );
        };
    }


}
