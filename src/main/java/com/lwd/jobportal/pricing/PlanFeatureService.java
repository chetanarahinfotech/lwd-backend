package com.lwd.jobportal.pricing;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.exception.FeatureAccessDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanFeatureService {

    private final PlanFeatureRepository repo;

    @Cacheable(value = "planFeatures", key = "#planId + '_' + #planType")
    public List<PlanFeature> getFeaturesByPlan(Long planId, PlanType planType) {
        return repo.findByPlanIdAndFeaturePlanTypeFetch(planId, planType);
    }

    @Cacheable(value = "planFeature", key = "#planId + '_' + #featureCode + '_' + #planType")
    public PlanFeature getFeature(Long planId, String featureCode, PlanType planType) {
        String normalizedCode = normalizeCode(featureCode);

        PlanFeature pf = repo.findByPlanIdAndFeatureCodeAndPlanTypeFetch(
                planId,
                normalizedCode,
                planType
        ).orElseThrow(() -> new FeatureAccessDeniedException(
                "Feature '" + normalizedCode + "' is not available for your plan"
        ));

        if (!Boolean.TRUE.equals(pf.getEnabled())) {
            throw new FeatureAccessDeniedException(
                    "Feature '" + normalizedCode + "' is disabled in your plan"
            );
        }

        return pf;
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new FeatureAccessDeniedException("Feature code is required");
        }

        String normalized = code.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new FeatureAccessDeniedException("Invalid feature code");
        }

        return normalized;
    }
}