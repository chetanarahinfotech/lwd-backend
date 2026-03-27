package com.lwd.jobportal.pricing.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.pricing.dto.CandidatePlanResponse;
import com.lwd.jobportal.pricing.dto.FeatureResponse;
import com.lwd.jobportal.pricing.dto.PlanRequest;
import com.lwd.jobportal.pricing.dto.PlanResponse;
import com.lwd.jobportal.pricing.dto.RecruiterPlanResponse;
import com.lwd.jobportal.pricing.entity.Plan;
import com.lwd.jobportal.pricing.entity.PlanFeature;
import com.lwd.jobportal.pricing.enums.PlanType;
import com.lwd.jobportal.pricing.repository.PlanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final PlanRepository planRepository;
    private final PlanFeatureService planFeatureService;

    @CacheEvict(value = {"candidatePlans", "recruiterPlans", "allPlans"}, allEntries = true)
    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.getName())
                .type(request.getType())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .isActive(request.getActive() != null ? request.getActive() : true)
                .build();

        planRepository.save(plan);

        log.info("Created plan id={}, name={}, type={}", plan.getId(), plan.getName(), plan.getType());
        return map(plan);
    }

    @CacheEvict(value = {"candidatePlans", "recruiterPlans", "allPlans"}, allEntries = true)
    @Transactional
    public PlanResponse updatePlan(Long planId, PlanRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        plan.setName(request.getName());
        plan.setType(request.getType());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        plan.setIsActive(request.getActive() != null ? request.getActive() : plan.getIsActive());

        planRepository.save(plan);

        log.info("Updated plan id={}, name={}, type={}", plan.getId(), plan.getName(), plan.getType());
        return map(plan);
    }

    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    
    @CacheEvict(value = {"candidatePlans", "recruiterPlans", "allPlans"}, allEntries = true)
    @Transactional
    public void togglePlan(Long planId, boolean active) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        plan.setIsActive(active);
        planRepository.save(plan);

        log.info("Toggled plan id={} to active={}", planId, active);
    }

    public List<CandidatePlanResponse> getCandidatePlans() {
        List<Plan> plans = planRepository.findByTypeAndIsActiveTrueOrderByPriceAsc(PlanType.JOB_SEEKER);
        log.info("Fetched {} active candidate plans", plans.size());

        return plans.stream()
                .map(this::toCandidatePlanResponse)
                .toList();
    }

    public List<RecruiterPlanResponse> getRecruiterPlans() {
        List<Plan> plans = planRepository.findByTypeAndIsActiveTrueOrderByPriceAsc(PlanType.RECRUITER);
        log.info("Fetched {} active recruiter plans", plans.size());

        return plans.stream()
                .map(this::toRecruiterPlanResponse)
                .toList();
    }

    private CandidatePlanResponse toCandidatePlanResponse(Plan plan) {
        return CandidatePlanResponse.builder()
                .planId(plan.getId())
                .planName(plan.getName())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .features(safeMapFeatures(plan.getId(), PlanType.JOB_SEEKER))
                .build();
    }

    private RecruiterPlanResponse toRecruiterPlanResponse(Plan plan) {
        return RecruiterPlanResponse.builder()
                .planId(plan.getId())
                .planName(plan.getName())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .features(safeMapFeatures(plan.getId(), PlanType.RECRUITER))
                .build();
    }

    /**
     * Safe wrapper so one broken feature row does not break the whole API.
     */
    private List<FeatureResponse> safeMapFeatures(Long planId, PlanType planType) {
        try {
            return mapFeatures(planId, planType);
        } catch (Exception ex) {
            log.error("Failed to map features for planId={}, planType={}", planId, planType, ex);
            return Collections.emptyList();
        }
    }

    /**
     * Null-safe feature mapping for production data.
     */
    private List<FeatureResponse> mapFeatures(Long planId, PlanType planType) {
        List<PlanFeature> planFeatures = planFeatureService.getFeaturesByPlan(planId, planType);

        if (planFeatures == null || planFeatures.isEmpty()) {
            log.warn("No plan features found for planId={}, planType={}", planId, planType);
            return Collections.emptyList();
        }

        return planFeatures.stream()
                .filter(pf -> {
                    if (pf == null) {
                        log.warn("Skipping null PlanFeature for planId={}, planType={}", planId, planType);
                        return false;
                    }

                    if (pf.getFeature() == null) {
                        log.warn("Skipping PlanFeature with null feature for planId={}, planType={}", planId, planType);
                        return false;
                    }

                    return true;
                })
                .map(pf -> FeatureResponse.builder()
                        .featureCode(pf.getFeature().getCode())
                        .enabled(pf.getEnabled())
                        .limitValue(pf.getLimitValue())
                        .limitType(pf.getLimitType())
                        .description(pf.getFeature().getDescription())
                        .build())
                .toList();
    }

    private PlanResponse map(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .type(plan.getType())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .active(plan.getIsActive())
                .build();
    }
}