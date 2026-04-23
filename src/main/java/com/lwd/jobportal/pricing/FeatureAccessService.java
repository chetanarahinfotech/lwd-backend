package com.lwd.jobportal.pricing;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.PlanUpgradeRequiredException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeatureAccessService {

    private final SubscriptionService subscriptionService;
    private final PlanFeatureService planFeatureService;
    private final UsageService usageService;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public void checkAccess(Long userId, String featureCode) {
        UserSubscription sub = subscriptionService.getActiveSubscription(userId);

        if (sub == null) {
            handleFreeAccess(userId, featureCode);
            return;
        }

        Long planId = sub.getPlan().getId();
        PlanType planType = sub.getPlan().getType();

        PlanFeature feature = planFeatureService.getFeature(planId, featureCode, planType);

        usageService.incrementWithLimit(
                userId,
                featureCode,
                feature.getLimitValue()
        );
    }

    public boolean hasAccess(Long userId, String featureCode) {
        try {
            UserSubscription sub = subscriptionService.getActiveSubscription(userId);

            if (sub == null) {
                return hasFreeAccess(userId, featureCode);
            }

            Long planId = sub.getPlan().getId();
            PlanType planType = sub.getPlan().getType();

            PlanFeature feature = planFeatureService.getFeature(planId, featureCode, planType);

            return Boolean.TRUE.equals(feature.getEnabled());
        } catch (Exception e) {
            return false;
        }
    }

    public void validatePlan(Long userId, PlanName[] allowedPlans) {
        if (allowedPlans == null || allowedPlans.length == 0) {
            return;
        }

        UserSubscription sub = subscriptionService.getActiveSubscription(userId);

        if (sub == null || sub.getPlan() == null) {
            throw new PlanUpgradeRequiredException("Active subscription required");
        }

        PlanName currentPlan = sub.getPlan().getName();

        for (PlanName plan : allowedPlans) {
            if (plan == currentPlan) {
                return;
            }
        }

        throw new PlanUpgradeRequiredException("Upgrade plan to access this feature");
    }

    private void handleFreeAccess(Long userId, String featureCode) {
        PlanType planType = getUserPlanType(userId);

        Plan freePlan = planRepository
                .findFirstByTypeAndName(planType, PlanName.FREE)
                .orElseThrow(() -> new ResourceNotFoundException("Free plan not configured for " + planType));

        PlanFeature feature = planFeatureService.getFeature(
                freePlan.getId(),
                featureCode,
                planType
        );

        usageService.incrementWithLimit(
                userId,
                featureCode,
                feature.getLimitValue()
        );
    }

    private boolean hasFreeAccess(Long userId, String featureCode) {
        try {
            PlanType planType = getUserPlanType(userId);

            Plan freePlan = planRepository
                    .findFirstByTypeAndName(planType, PlanName.FREE)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Free plan not configured for " + planType)
                    );

            PlanFeature feature = planFeatureService.getFeature(
                    freePlan.getId(),
                    featureCode,
                    planType
            );

            return Boolean.TRUE.equals(feature.getEnabled());

        } catch (Exception e) {
            return false;
        }
    }

    private PlanType getUserPlanType(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = user.getRole();

        return switch (role) {
            case RECRUITER -> PlanType.RECRUITER;
            case COMPANY_ADMIN -> PlanType.COMPANY_ADMIN;
            case JOB_SEEKER -> PlanType.JOB_SEEKER;
            default -> throw new IllegalArgumentException("Unsupported role for plan type: " + role);
        };
    }
}