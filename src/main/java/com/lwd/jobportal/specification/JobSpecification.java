package com.lwd.jobportal.specification;

import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.enums.JobType;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> searchJobs(
            String keyword,
            String location,
            String companyName,
            Integer minExp,
            Integer maxExp,
            JobType jobType
    ) {
    	return (root, query, cb) -> {

    	    query.distinct(true);   // 🔥 Prevent duplicate jobs

    	    List<Predicate> predicates = new ArrayList<>();

    	    // Use LEFT JOIN once
    	    var companyJoin = root.join("company", jakarta.persistence.criteria.JoinType.LEFT);

    	    if (keyword != null && !keyword.isEmpty()) {

    	        String pattern = "%" + keyword.toLowerCase() + "%";

    	        Predicate titleMatch =
    	                cb.like(cb.lower(root.get("title")), pattern);

    	        Predicate locationMatch =
    	                cb.like(cb.lower(root.get("location")), pattern);

    	        Predicate companyMatch =
    	                cb.like(cb.lower(companyJoin.get("companyName")), pattern);

    	        Predicate jobTypeMatch =
    	                cb.like(cb.lower(root.get("jobType").as(String.class)), pattern);

    	        predicates.add(cb.or(titleMatch, locationMatch, companyMatch, jobTypeMatch));
    	    }

    	    if (location != null && !location.isEmpty()) {
    	        predicates.add(cb.like(cb.lower(root.get("location")),
    	                "%" + location.toLowerCase() + "%"));
    	    }

    	    if (companyName != null && !companyName.isEmpty()) {
    	        predicates.add(cb.like(cb.lower(companyJoin.get("companyName")),
    	                "%" + companyName.toLowerCase() + "%"));
    	    }

    	    if (minExp != null) {
    	        predicates.add(cb.greaterThanOrEqualTo(root.get("minExperience"), minExp));
    	    }

    	    if (maxExp != null) {
    	        predicates.add(cb.lessThanOrEqualTo(root.get("maxExperience"), maxExp));
    	    }

    	    if (jobType != null) {
    	        predicates.add(cb.equal(root.get("jobType"), jobType));
    	    }

    	    return cb.and(predicates.toArray(new Predicate[0]));
    	};

    }
    
    
    public static Specification<Job> filterJobs(
            String location,
            JobType jobType,
            Integer minExp,
            Integer maxExp,
            JobStatus status
    ) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (location != null)
                p = cb.and(p, cb.equal(root.get("location"), location));

            if (jobType != null)
                p = cb.and(p, cb.equal(root.get("jobType"), jobType));

            if (minExp != null)
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("minExperience"), minExp));

            if (maxExp != null)
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("maxExperience"), maxExp));

            if (status != null)
                p = cb.and(p, cb.equal(root.get("status"), status));

            return p;
        };
    }

}
