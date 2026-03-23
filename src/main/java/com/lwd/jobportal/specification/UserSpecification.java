package com.lwd.jobportal.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.lwd.jobportal.dto.admin.UserSearchRequest;
import com.lwd.jobportal.entity.User;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> searchUsers(UserSearchRequest request) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            // keyword: name / email / phone
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword),
                        cb.like(cb.lower(root.get("phone")), keyword)
                ));
            }

            // role
            if (request.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), request.getRole()));
            }

            // status
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // active
            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            // email verified
            if (request.getEmailVerified() != null) {
                predicates.add(cb.equal(root.get("emailVerified"), request.getEmailVerified()));
            }

            // locked
            if (request.getLocked() != null) {
                predicates.add(cb.equal(root.get("locked"), request.getLocked()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}