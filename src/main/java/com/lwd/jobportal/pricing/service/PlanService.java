package com.lwd.jobportal.pricing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.pricing.dto.CandidatePlanResponse;
import com.lwd.jobportal.pricing.dto.FeatureResponse;
import com.lwd.jobportal.pricing.dto.PlanRequest;
import com.lwd.jobportal.pricing.dto.PlanResponse;
import com.lwd.jobportal.pricing.dto.RecruiterPlanResponse;
import com.lwd.jobportal.pricing.entity.Plan;
import com.lwd.jobportal.pricing.enums.PlanType;
import com.lwd.jobportal.pricing.repository.PlanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanFeatureService planFeatureService;

    // ✅ Create Plan
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

        return map(plan);
    }

    // 🔄 Update Plan
    @Transactional
    public PlanResponse updatePlan(Long planId, PlanRequest request) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        plan.setName(request.getName());
        plan.setType(request.getType());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        plan.setIsActive(request.getActive());

        planRepository.save(plan);

        return map(plan);
    }

    // 📊 Get All Plans
    public List<PlanResponse> getAllPlans() {

        return planRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }
    


    // ❌ Disable Plan
    @Transactional
    public void togglePlan(Long planId, boolean active) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        plan.setIsActive(active);
        planRepository.save(plan);
    }
    
    
    // ===============================
    // 👤 Candidate Plans
    // ===============================
    public List<CandidatePlanResponse> getCandidatePlans() {

        List<Plan> plans = planRepository
                .findByTypeAndIsActiveTrueOrderByPriceAsc(PlanType.JOB_SEEKER);

        return plans.stream()
                .map(plan -> CandidatePlanResponse.builder()
                        .planId(plan.getId())
                        .planName(plan.getName())
                        .price(plan.getPrice())
                        .durationDays(plan.getDurationDays())
                        .features(mapFeatures(plan.getId(), PlanType.JOB_SEEKER)) // ✅ pass type
                        .build()
                )
                .toList();
    }


    // ===============================
    // 🏢 Recruiter Plans
    // ===============================
    public List<RecruiterPlanResponse> getRecruiterPlans() {

        List<Plan> plans = planRepository
                .findByTypeAndIsActiveTrueOrderByPriceAsc(PlanType.RECRUITER);

        return plans.stream()
                .map(plan -> RecruiterPlanResponse.builder()
                        .planId(plan.getId())
                        .planName(plan.getName())
                        .price(plan.getPrice())
                        .durationDays(plan.getDurationDays())
                        .features(mapFeatures(plan.getId(), PlanType.RECRUITER)) // ✅ pass type
                        .build()
                )
                .toList();
    }


    // ===============================
    // 🔁 Feature Mapper (COMMON)
    // ===============================
    private List<FeatureResponse> mapFeatures(Long planId, PlanType planType) {

        return planFeatureService.getFeaturesByPlan(planId, planType)
                .stream()
                .map(pf -> FeatureResponse.builder()
                        .featureCode(pf.getFeature().getCode())
                        .enabled(pf.getEnabled())
                        .limitValue(pf.getLimitValue())
                        .limitType(pf.getLimitType())
                        .description(pf.getFeature().getDescription()) // 🔥 added
                        .build()
                )
                .toList();
    }



    // 🔁 Mapper
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
