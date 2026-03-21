package com.lwd.jobportal.pricing.service;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.pricing.annotation.RequiresFeature;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureCheckAspect {

    private final FeatureAccessService accessService;

    @Before("@annotation(requiresFeature)")
    public void check(RequiresFeature requiresFeature) {

        // 🔥 0. ADMIN BYPASS
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        Long userId = SecurityUtils.getUserId();

        String featureCode = requiresFeature.value();

        // 🔥 1. ROLE CHECK
        if (requiresFeature.roles().length > 0) {

            boolean allowed = false;

            for (Role role : requiresFeature.roles()) {
                if (SecurityUtils.hasRole(role)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                throw new AccessDeniedException("Access denied: role not allowed");
            }
        }

        // 🔥 2. PLAN CHECK
        accessService.validatePlan(userId, requiresFeature.plans());

        // 🔥 3. FEATURE + LIMIT + INCREMENT (ALL-IN-ONE)
        accessService.checkAccess(userId, featureCode);
    }
}
