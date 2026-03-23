package com.lwd.jobportal.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.lwd.jobportal.dto.jobapplicationdto.ApplicationSearchRequest;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.entity.JobApplication;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class JobApplicationSpecification {

    public static Specification<JobApplication> searchApplications(
            Long userId,
            Role role,
            ApplicationSearchRequest request
    ) {
        return (root, query, cb) -> {
            query.distinct(true);

            Join<JobApplication, Job> jobJoin = root.join("job", JoinType.LEFT);
            Join<Job, Company> companyJoin = jobJoin.join("company", JoinType.LEFT);
            Join<JobApplication, User> jobSeekerJoin = root.join("jobSeeker", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            // ================= ROLE BASED DATA ACCESS =================
            if (role == Role.ADMIN) {
                // all applications
            } else if (role == Role.RECRUITER_ADMIN) {
                predicates.add(cb.equal(companyJoin.get("createdById"), userId));
            } else if (role == Role.RECRUITER) {
                predicates.add(cb.equal(jobJoin.get("createdBy").get("id"), userId));
            } else {
                predicates.add(cb.disjunction()); // no data
            }

            // ================= KEYWORD SEARCH =================
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";

                Predicate candidateNameMatch = cb.like(cb.lower(root.get("fullName")), keyword);
                Predicate jobNameMatch = cb.like(cb.lower(jobJoin.get("title")), keyword);
                Predicate companyNameMatch = cb.like(cb.lower(companyJoin.get("companyName")), keyword);

                predicates.add(cb.or(candidateNameMatch, jobNameMatch, companyNameMatch));
            }

            // ================= FILTERS =================
            if (request.getApplicationSource() != null) {
                predicates.add(cb.equal(root.get("applicationSource"), request.getApplicationSource()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getSkills() != null && !request.getSkills().trim().isEmpty()) {
                predicates.add(
                    cb.like(cb.lower(root.get("skills")), "%" + request.getSkills().trim().toLowerCase() + "%")
                );
            }

            // ================= DATE FILTER =================
            if (request.getDateFilter() != null && !request.getDateFilter().isBlank()) {
                LocalDateTime start = null;
                LocalDateTime end = null;

                switch (request.getDateFilter().toUpperCase()) {
                    case "TODAY" -> {
                        LocalDate today = LocalDate.now();
                        start = today.atStartOfDay();
                        end = today.atTime(LocalTime.MAX);
                    }
                    case "LAST_WEEK" -> {
                        LocalDate today = LocalDate.now();
                        start = today.minusDays(7).atStartOfDay();
                        end = today.atTime(LocalTime.MAX);
                    }
                    case "LAST_MONTH" -> {
                        LocalDate today = LocalDate.now();
                        start = today.minusMonths(1).atStartOfDay();
                        end = today.atTime(LocalTime.MAX);
                    }
                    case "SPECIFIC_DATE" -> {
                        if (request.getSpecificDate() != null) {
                            start = request.getSpecificDate().atStartOfDay();
                            end = request.getSpecificDate().atTime(LocalTime.MAX);
                        }
                    }
                    case "CUSTOM_RANGE" -> {
                        if (request.getStartDate() != null && request.getEndDate() != null) {
                            start = request.getStartDate().atStartOfDay();
                            end = request.getEndDate().atTime(LocalTime.MAX);
                        }
                    }
                }

                if (start != null && end != null) {
                    predicates.add(cb.between(root.get("appliedAt"), start, end));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}