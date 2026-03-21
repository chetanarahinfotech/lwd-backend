package com.lwd.jobportal.pricing.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.exception.FeatureAccessDeniedException;
import com.lwd.jobportal.pricing.entity.PlanFeature;
import com.lwd.jobportal.pricing.enums.PlanType;
import com.lwd.jobportal.pricing.repository.PlanFeatureRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class PlanFeatureService {

    private final PlanFeatureRepository repo;

    // ⚡ Cache ALL features per plan
    @Cacheable(value = "planFeatures", key = "#planId + '_' + #planType")
    public List<PlanFeature> getFeaturesByPlan(Long planId, PlanType planType) {

        return repo.findByPlanIdAndFeature_PlanType(planId, planType);
    }

    // 🔍 Find specific feature
    @Cacheable(value = "planFeature", key = "#planId + '_' + #featureCode + '_' + #planType")
    public PlanFeature getFeature(Long planId, String featureCode, PlanType planType) {

        PlanFeature pf = repo
                .findByPlanIdAndFeature_CodeAndFeature_PlanType(planId, featureCode, planType)
                .orElseThrow(() -> new FeatureAccessDeniedException(
                        "Feature '" + featureCode + "' is not available for your plan"
                ));

        // 🔥 Extra Safety
        if (!Boolean.TRUE.equals(pf.getEnabled())) {
            throw new FeatureAccessDeniedException(
                    "Feature '" + featureCode + "' is disabled in your plan"
            );
        }

        return pf;
    }
}
