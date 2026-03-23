package com.lwd.jobportal.specification;

import com.lwd.jobportal.dto.jobdto.JobSearchRequest;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobSeeker;
import com.lwd.jobportal.entity.Skill;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;
import com.lwd.jobportal.enums.NoticeStatus;
import com.lwd.jobportal.enums.Role;

import org.springframework.data.jpa.domain.Specification;

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

                // 🔥 join instead of fetch
                root.join("createdBy", JoinType.LEFT);

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

                root.fetch("company", JoinType.LEFT);

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

    
    
    public static Specification<Job> recommendedJobs(JobSeeker seeker) {
        return (root, query, cb) -> {

            boolean isCountQuery = query.getResultType() == Long.class;

//            if (!isCountQuery) {
//                root.fetch("company", JoinType.LEFT);
//                query.distinct(true);
//            }

            List<Predicate> predicates = new ArrayList<>();

            // ✅ Only active public jobs
            predicates.add(cb.isFalse(root.get("deleted")));
            predicates.add(cb.equal(root.get("status"), JobStatus.OPEN));

            // ✅ Experience match
            if (seeker.getTotalExperience() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("minExperience"),
                        seeker.getTotalExperience()
                    )
                );

                predicates.add(
                    cb.or(
                        cb.isNull(root.get("maxExperience")),
                        cb.greaterThanOrEqualTo(
                            root.get("maxExperience"),
                            seeker.getTotalExperience()
                        )
                    )
                );
            }

            // ✅ Salary / CTC match
            if (seeker.getExpectedCTC() != null) {
                predicates.add(
                    cb.or(
                        cb.isNull(root.get("maxSalary")),
                        cb.greaterThanOrEqualTo(
                            root.get("maxSalary"),
                            seeker.getExpectedCTC()
                        )
                    )
                );
            }

            // ✅ Preferred location
//            if (seeker.getPreferredLocation() != null &&
//                !seeker.getPreferredLocation().isBlank()) {
//
//                predicates.add(
//                    cb.like(
//                        cb.lower(root.get("location")),
//                        "%" + seeker.getPreferredLocation().trim().toLowerCase() + "%"
//                    )
//                );
//            }

            // ✅ Skills match
//            if (seeker.getSkills() != null && !seeker.getSkills().isEmpty()) {
//                Predicate skillsPredicate = cb.disjunction();
//
//                for (Skill skill : seeker.getSkills()) {
//                    if (skill != null &&
//                        skill.getName() != null &&
//                        !skill.getName().isBlank()) {
//
//                        skillsPredicate = cb.or(
//                            skillsPredicate,
//                            cb.like(
//                                cb.lower(root.get("skills")),
//                                "%" + skill.getName().trim().toLowerCase() + "%"
//                            )
//                        );
//                    }
//                }
//
//                predicates.add(skillsPredicate);
//            }

            // ✅ Notice period compatibility
            if (seeker.getNoticePeriod() != null) {
                predicates.add(
                    cb.or(
                        cb.isNull(root.get("maxNoticePeriod")),
                        cb.greaterThanOrEqualTo(
                            root.get("maxNoticePeriod"),
                            seeker.getNoticePeriod()
                        )
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Job> searchJobsByRole(
            Long userId,
            Long companyId,
            Role role,
            JobSearchRequest request
    ) {
        return (root, query, cb) -> {

            query.distinct(true);

            Join<Job, Company> companyJoin = root.join("company", JoinType.LEFT);
            Join<Job, User> createdByJoin = root.join("createdBy", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            // ================= BASE =================
            predicates.add(cb.isFalse(root.get("deleted")));

            // ================= ROLE SCOPE =================
            if (role == Role.ADMIN) {
                // no restriction
            } 
            else if (role == Role.RECRUITER_ADMIN) {
                predicates.add(cb.equal(companyJoin.get("id"), companyId));
            } 
            else if (role == Role.RECRUITER) {
                predicates.add(cb.equal(createdByJoin.get("id"), userId));
            } 
            else {
                predicates.add(cb.disjunction());
            }

            // ================= KEYWORD =================
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {

                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword),
                        cb.like(cb.lower(root.get("location")), keyword),
                        cb.like(cb.lower(root.get("industry")), keyword),
                        cb.like(cb.lower(root.get("skills")), keyword),
                        cb.like(cb.lower(companyJoin.get("companyName")), keyword)
                ));
            }

            // ================= FILTERS =================

            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("location")),
                        "%" + request.getLocation().toLowerCase() + "%"
                ));
            }

            if (request.getIndustry() != null && !request.getIndustry().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("industry")),
                        "%" + request.getIndustry().toLowerCase() + "%"
                ));
            }

            if (request.getSkills() != null && !request.getSkills().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("skills")),
                        "%" + request.getSkills().toLowerCase() + "%"
                ));
            }

            // ENUMS
            if (request.getJobType() != null) {
                predicates.add(cb.equal(root.get("jobType"), request.getJobType()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getApplicationSource() != null) {
                predicates.add(cb.equal(root.get("applicationSource"), request.getApplicationSource()));
            }

            // EXPERIENCE
            if (request.getMinExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minExperience"), request.getMinExperience()));
            }

            if (request.getMaxExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxExperience"), request.getMaxExperience()));
            }

            // SALARY
            if (request.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minSalary"), request.getMinSalary()));
            }

            if (request.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxSalary"), request.getMaxSalary()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
