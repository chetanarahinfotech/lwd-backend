package com.lwd.jobportal.pricing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, Long> {

    Optional<PlanFeature> findByPlanIdAndFeatureId(Long planId, Long featureId);

    List<PlanFeature> findAllByPlanId(Long planId);

    @Query("""
        SELECT pf
        FROM PlanFeature pf
        JOIN FETCH pf.feature f
        WHERE pf.plan.id = :planId
          AND f.planType = :planType
    """)
    List<PlanFeature> findByPlanIdAndFeaturePlanTypeFetch(
            @Param("planId") Long planId,
            @Param("planType") PlanType planType
    );

    @Query("""
        SELECT pf
        FROM PlanFeature pf
        JOIN FETCH pf.feature f
        WHERE pf.plan.id = :planId
          AND f.code = :featureCode
          AND f.planType = :planType
    """)
    Optional<PlanFeature> findByPlanIdAndFeatureCodeAndPlanTypeFetch(
            @Param("planId") Long planId,
            @Param("featureCode") String featureCode,
            @Param("planType") PlanType planType
    );
}