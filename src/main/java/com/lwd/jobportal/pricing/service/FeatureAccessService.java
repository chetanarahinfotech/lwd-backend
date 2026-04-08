package com.lwd.jobportal.pricing.service;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.exception.FeatureAccessDeniedException;
import com.lwd.jobportal.exception.PlanUpgradeRequiredException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.pricing.entity.Plan;
import com.lwd.jobportal.pricing.entity.PlanFeature;
import com.lwd.jobportal.pricing.entity.UserSubscription;
import com.lwd.jobportal.pricing.enums.PlanName;
import com.lwd.jobportal.pricing.enums.PlanType;
import com.lwd.jobportal.pricing.repository.PlanRepository;
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

        // 1️⃣ Get active subscription
        UserSubscription sub =
                subscriptionService.getActiveSubscription(userId);

        // 🔥 2️⃣ FREE PLAN FALLBACK
        if (sub == null) {
            handleFreeAccess(userId, featureCode);
            return;
        }

        Long planId = sub.getPlan().getId();
        PlanType planType = sub.getPlan().getType(); // 🔥 IMPORTANT

        // 🔥 3️⃣ Get feature config (with planType)
        PlanFeature feature =
                planFeatureService.getFeature(
                        planId,
                        featureCode,
                        planType // ✅ FIXED
                );

        // 4️⃣ Enabled check
        if (!Boolean.TRUE.equals(feature.getEnabled())) {
            throw new FeatureAccessDeniedException("Feature disabled for your plan");
        }

        // 🔥 5️⃣ Limit check + increment
        usageService.incrementWithLimit(
                userId,
                featureCode,
                feature.getLimitValue()
        );
    }
    
    
    public boolean hasAccess(Long userId, String featureCode) {
        try {
            UserSubscription sub = subscriptionService.getActiveSubscription(userId);
            System.out.println("hasAccess userId=" + userId + ", feature=" + featureCode);
            System.out.println("subscription=" + (sub != null ? sub.getId() : null));

            if (sub == null) {
                System.out.println("No active subscription, checking free plan");
                return hasFreeAccess(userId, featureCode);
            }

            Long planId = sub.getPlan().getId();
            PlanType planType = sub.getPlan().getType();

            System.out.println("planId=" + planId + ", planType=" + planType + ", planName=" + sub.getPlan().getName());

            PlanFeature feature = planFeatureService.getFeature(planId, featureCode, planType);

            System.out.println("feature enabled=" + feature.getEnabled());

            return Boolean.TRUE.equals(feature.getEnabled());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    
    
    public void validatePlan(Long userId, PlanName[] allowedPlans) {

        if (allowedPlans == null || allowedPlans.length == 0) {
            return;
        }

        PlanName currentPlan = subscriptionService
                .getActiveSubscription(userId)
                .getPlan()
                .getName();

        for (PlanName plan : allowedPlans) {
            if (plan == currentPlan) {
                return;
            }
        }

        throw new PlanUpgradeRequiredException("Upgrade plan to access this feature");
    }
    
    private void handleFreeAccess(Long userId, String featureCode) {

        // 1️⃣ Get user plan type (JOB_SEEKER / RECRUITER)
        PlanType planType = getUserPlanType(userId);

        // 2️⃣ Get FREE plan for that type
        Plan freePlan = planRepository
                .findFirstByTypeAndName(planType, PlanName.FREE)
                .orElseThrow(() -> new ResourceNotFoundException("Free plan not configured for " + planType));



        // 3️⃣ Get feature config
        PlanFeature feature = planFeatureService.getFeature(
                freePlan.getId(),
                featureCode,
                planType
        );

        // 4️⃣ Check enabled
        if (!Boolean.TRUE.equals(feature.getEnabled())) {
            throw new PlanUpgradeRequiredException("Upgrade plan to access this feature");
        }

        // 5️⃣ Apply usage limit
        usageService.incrementWithLimit(
                userId,
                featureCode,
                feature.getLimitValue()
        );
    }
    
    private boolean hasFreeAccess(Long userId, String featureCode) {
        try {
            // 1️⃣ Get user plan type
            PlanType planType = getUserPlanType(userId);

            // 2️⃣ Get FREE plan for that type
            Plan freePlan = planRepository
                    .findFirstByTypeAndName(planType, PlanName.FREE)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Free plan not configured for " + planType)
                    );

            // 3️⃣ Get feature config
            PlanFeature feature = planFeatureService.getFeature(
                    freePlan.getId(),
                    featureCode,
                    planType
            );

            // 4️⃣ Enabled check only
            return Boolean.TRUE.equals(feature.getEnabled());

        } catch (Exception e) {
            return false;
        }
    }


    private PlanType getUserPlanType(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return user.getRole() == Role.RECRUITER
                ? PlanType.RECRUITER
                : PlanType.JOB_SEEKER;
    }


}
