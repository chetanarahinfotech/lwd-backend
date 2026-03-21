package com.lwd.jobportal.pricing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lwd.jobportal.pricing.entity.PlanFeature;
import com.lwd.jobportal.pricing.enums.PlanType;

public interface PlanFeatureRepository
        extends JpaRepository<PlanFeature, Long> {

    // 🔥 Get specific feature
    @Query("""
        SELECT pf FROM PlanFeature pf
        JOIN FETCH pf.feature f
        WHERE pf.plan.id = :planId
        AND f.code = :featureCode
    """)
    Optional<PlanFeature> findByPlanAndFeature(
            Long planId,
            String featureCode
    );

    // ⚡ Get all features for a plan (cache this)
    @Query("""
        SELECT pf FROM PlanFeature pf
        JOIN FETCH pf.feature f
        WHERE pf.plan.id = :planId
    """)
    List<PlanFeature> findAllByPlanId(Long planId);
    
    Optional<PlanFeature> findByPlanIdAndFeatureId(Long planId, Long featureId);
    
    Optional<PlanFeature> findByPlanIdAndFeature_Code(Long planId, String code);
    
    List<PlanFeature> findByPlanIdAndFeature_PlanType(Long planId, PlanType planType);

    Optional<PlanFeature> findByPlanIdAndFeature_CodeAndFeature_PlanType(
            Long planId,
            String code,
            PlanType planType
    );

}
