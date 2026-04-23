package com.lwd.jobportal.pricing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByTypeAndIsActiveTrue(PlanType type);
    List<Plan> findByTypeAndIsActiveTrueOrderByPriceAsc(PlanType type);
    Optional<Plan> findFirstByTypeAndName(PlanType type, PlanName name);



}
