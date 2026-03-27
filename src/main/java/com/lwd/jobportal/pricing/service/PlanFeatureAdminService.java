package com.lwd.jobportal.pricing.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.pricing.dto.BulkPlanFeatureRequest;
import com.lwd.jobportal.pricing.dto.PlanFeatureRequest;
import com.lwd.jobportal.pricing.entity.Feature;
import com.lwd.jobportal.pricing.entity.Plan;
import com.lwd.jobportal.pricing.entity.PlanFeature;
import com.lwd.jobportal.pricing.repository.FeatureRepository;
import com.lwd.jobportal.pricing.repository.PlanFeatureRepository;
import com.lwd.jobportal.pricing.repository.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanFeatureAdminService {

    private final PlanRepository planRepository;
    private final FeatureRepository featureRepository;
    private final PlanFeatureRepository planFeatureRepository;

    /**
     * Upsert a single feature for a plan
     */
    @CacheEvict(
    	    value = {"planFeatures", "planFeature", "candidatePlans", "recruiterPlans", "allPlans"},
    	    allEntries = true
    	)
    @Transactional
    public void upsertFeature(Long planId, PlanFeatureRequest request) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        Feature feature = featureRepository.findByCode(request.getFeatureCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feature not found: " + request.getFeatureCode()
                ));

        // 🔥 VALIDATION
        if (feature.getPlanType() != plan.getType()) {
            throw new InvalidOperationException(
                    "Feature " + feature.getCode() + " does not belong to plan type " + plan.getType()
            );
        }

        PlanFeature pf = planFeatureRepository
                .findByPlanIdAndFeatureId(planId, feature.getId())
                .orElse(null);

        if (pf == null) {
            pf = PlanFeature.builder()
                    .plan(plan)
                    .feature(feature)
                    .enabled(Boolean.TRUE.equals(request.getEnabled()))
                    .limitValue(request.getLimitValue())
                    .limitType(request.getLimitType())
                    .build();
        } else {
            pf.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
            pf.setLimitValue(request.getLimitValue());
            pf.setLimitType(request.getLimitType());
        }

        planFeatureRepository.save(pf);
    }



    /**
     * Bulk update features for a plan
     */
    @CacheEvict(
    	    value = {"planFeatures", "planFeature", "candidatePlans", "recruiterPlans", "allPlans"},
    	    allEntries = true
    	)
    @Transactional
    public void upsertFeaturesBulk(Long planId, BulkPlanFeatureRequest bulkRequest) {

        if (bulkRequest.getFeatures() == null || bulkRequest.getFeatures().isEmpty()) {
            throw new InvalidOperationException("No features provided");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        List<String> codes = bulkRequest.getFeatures()
                .stream()
                .map(PlanFeatureRequest::getFeatureCode)
                .toList();

        List<Feature> features = featureRepository.findByCodeIn(codes);

        Map<String, Feature> featureMap = features.stream()
                .collect(Collectors.toMap(Feature::getCode, f -> f));

        for (PlanFeatureRequest request : bulkRequest.getFeatures()) {

            Feature feature = featureMap.get(request.getFeatureCode());

            if (feature == null) {
                throw new ResourceNotFoundException(
                        "Feature not found: " + request.getFeatureCode()
                );
            }

            if (feature.getPlanType() != plan.getType()) {
                throw new InvalidOperationException(
                        "Feature " + feature.getCode() + " not valid for " + plan.getType()
                );
            }

            PlanFeature pf = planFeatureRepository
                    .findByPlanIdAndFeatureId(planId, feature.getId())
                    .orElse(null);

            if (pf == null) {
                pf = PlanFeature.builder()
                        .plan(plan)
                        .feature(feature)
                        .enabled(Boolean.TRUE.equals(request.getEnabled()))
                        .limitValue(request.getLimitValue())
                        .limitType(request.getLimitType())
                        .build();
            } else {
                pf.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
                pf.setLimitValue(request.getLimitValue());
                pf.setLimitType(request.getLimitType());
            }

            planFeatureRepository.save(pf);
        }
    }


    
    
    // 🔹 Fetch all features for a plan
    public List<PlanFeatureRequest> getPlanFeaturesForPlan(Long planId) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        return planFeatureRepository.findAllByPlanId(planId)
                .stream()
                .filter(pf -> pf.getFeature().getPlanType() == plan.getType()) // 🔥 FIX
                .map(f -> {
                    PlanFeatureRequest req = new PlanFeatureRequest();
                    req.setFeatureCode(f.getFeature().getCode());
                    req.setEnabled(f.getEnabled());
                    req.setLimitValue(f.getLimitValue());
                    req.setLimitType(f.getLimitType());
                    req.setDescription(f.getFeature().getDescription()); // optional
                    return req;
                })
                .collect(Collectors.toList());
    }
    
    public List<PlanFeatureRequest> getAllFeaturesByPlanType(Long planId) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        // ✅ Step 1: Get all features of this plan type
        List<Feature> features = featureRepository.findByPlanType(plan.getType());

        // ✅ Step 2: Get existing plan feature mappings
        List<PlanFeature> existing = planFeatureRepository.findAllByPlanId(planId);

        Map<Long, PlanFeature> existingMap = existing.stream()
                .collect(Collectors.toMap(pf -> pf.getFeature().getId(), pf -> pf));

        // ✅ Step 3: Merge (important)
        return features.stream().map(feature -> {

            PlanFeature pf = existingMap.get(feature.getId());

            PlanFeatureRequest req = new PlanFeatureRequest();
            req.setFeatureCode(feature.getCode());
            req.setDescription(feature.getDescription());

            if (pf != null) {
                req.setEnabled(pf.getEnabled());
                req.setLimitValue(pf.getLimitValue());
                req.setLimitType(pf.getLimitType());
            } else {
                // 🔥 Default values if not configured yet
                req.setEnabled(false);
                req.setLimitValue(null);
                req.setLimitType(null);
            }

            return req;

        }).toList();
    }


}
