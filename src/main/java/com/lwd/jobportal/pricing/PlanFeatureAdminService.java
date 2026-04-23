package com.lwd.jobportal.pricing;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;

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
        value = {
            "planFeatures",
            "planFeature",
            "candidatePlans",
            "recruiterPlans",
            "CompanyAdminPlans",
            "allPlans"
        },
        allEntries = true
    )
    @Transactional
    public void upsertFeature(Long planId, PlanFeatureRequest request) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        String normalizedCode = normalizeCode(request.getFeatureCode());

        Feature feature = getAllowedFeatureByCodeAndPlanType(normalizedCode, plan.getType());

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
        value = {
            "planFeatures",
            "planFeature",
            "candidatePlans",
            "recruiterPlans",
            "CompanyAdminPlans",
            "allPlans"
        },
        allEntries = true
    )
    @Transactional
    public void upsertFeaturesBulk(Long planId, BulkPlanFeatureRequest bulkRequest) {

        if (bulkRequest.getFeatures() == null || bulkRequest.getFeatures().isEmpty()) {
            throw new InvalidOperationException("No features provided");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        List<PlanType> allowedPlanTypes = getAllowedFeatureTypes(plan.getType());

        List<String> normalizedCodes = bulkRequest.getFeatures()
                .stream()
                .map(PlanFeatureRequest::getFeatureCode)
                .map(this::normalizeCode)
                .distinct()
                .toList();

        List<Feature> features = featureRepository.findByCodeInAndPlanTypeIn(normalizedCodes, allowedPlanTypes);

        Map<String, Feature> featureMap = features.stream()
                .collect(Collectors.toMap(
                        f -> buildFeatureMapKey(f.getCode(), f.getPlanType()),
                        f -> f,
                        (existing, replacement) -> choosePreferredFeature(existing, replacement, plan.getType())
                ));

        for (PlanFeatureRequest request : bulkRequest.getFeatures()) {

            String normalizedCode = normalizeCode(request.getFeatureCode());

            Feature feature = resolveFeatureFromMap(featureMap, normalizedCode, plan.getType());

            if (feature == null) {
                throw new ResourceNotFoundException(
                        "Feature not found or not allowed for plan type " + plan.getType() + ": " + normalizedCode
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

    /**
     * Fetch configured features already mapped for a plan
     */
    public List<PlanFeatureRequest> getPlanFeaturesForPlan(Long planId) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        return planFeatureRepository.findAllByPlanId(planId)
                .stream()
                .filter(pf -> isFeatureAllowedForPlan(
                        pf.getFeature().getPlanType(),
                        plan.getType()
                ))
                .map(f -> {
                    PlanFeatureRequest req = new PlanFeatureRequest();
                    req.setFeatureCode(f.getFeature().getCode());
                    req.setEnabled(f.getEnabled());
                    req.setLimitValue(f.getLimitValue());
                    req.setLimitType(f.getLimitType());
                    req.setDescription(f.getFeature().getDescription());
                    return req;
                })
                .collect(Collectors.toList());
    }

    /**
     * Fetch all available features by plan type, merged with current configuration
     */
    public List<PlanFeatureRequest> getAllFeaturesByPlanType(Long planId) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        List<PlanType> allowedPlanTypes = getAllowedFeatureTypes(plan.getType());

        List<Feature> features = featureRepository.findByPlanTypeIn(allowedPlanTypes);

        List<PlanFeature> existing = planFeatureRepository.findAllByPlanId(planId);

        Map<Long, PlanFeature> existingMap = existing.stream()
                .collect(Collectors.toMap(pf -> pf.getFeature().getId(), pf -> pf));

        return features.stream()
                .filter(feature -> isFeatureAllowedForPlan(feature.getPlanType(), plan.getType()))
                .map(feature -> {
                    PlanFeature pf = existingMap.get(feature.getId());

                    PlanFeatureRequest req = new PlanFeatureRequest();
                    req.setFeatureCode(feature.getCode());
                    req.setDescription(feature.getDescription());

                    if (pf != null) {
                        req.setEnabled(pf.getEnabled());
                        req.setLimitValue(pf.getLimitValue());
                        req.setLimitType(pf.getLimitType());
                    } else {
                        req.setEnabled(false);
                        req.setLimitValue(null);
                        req.setLimitType(null);
                    }

                    return req;
                })
                .toList();
    }

    /**
     * Validation + inheritance rules
     */  
    private boolean isFeatureAllowedForPlan(PlanType featureType, PlanType planType) {
        return featureType == planType;
    }

    /**
     * Which feature types are valid for the given plan
     */
    private List<PlanType> getAllowedFeatureTypes(PlanType planType) {
        return List.of(planType);
    }

    /**
     * Find exactly one allowed feature for a code + plan type
     */
    private Feature getAllowedFeatureByCodeAndPlanType(String code, PlanType planType) {
        List<PlanType> allowedTypes = getAllowedFeatureTypes(planType);

        List<Feature> features = featureRepository.findByCodeAndPlanTypeIn(code, allowedTypes);

        if (features == null || features.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Feature not found or not allowed for plan type " + planType + ": " + code
            );
        }

        return features.stream()
                .reduce((current, next) -> choosePreferredFeature(current, next, planType))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feature not found or not allowed for plan type " + planType + ": " + code
                ));
    }

    /**
     * Prefer exact match over inherited match
     */
    private Feature choosePreferredFeature(Feature current, Feature next, PlanType planType) {
        if (current.getPlanType() == planType) return current;
        if (next.getPlanType() == planType) return next;
        return current;
    }

    private String buildFeatureMapKey(String code, PlanType planType) {
        return code + "::" + planType.name();
    }

    private Feature resolveFeatureFromMap(Map<String, Feature> featureMap, String code, PlanType planType) {
        Feature exact = featureMap.get(buildFeatureMapKey(code, planType));
        if (exact != null) return exact;

        if (planType == PlanType.COMPANY_ADMIN) {
            return featureMap.get(buildFeatureMapKey(code, PlanType.RECRUITER));
        }

        return null;
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidOperationException("Feature code is required");
        }

        String normalized = code.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new InvalidOperationException("Invalid feature code");
        }

        return normalized;
    }
}