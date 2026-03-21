package com.lwd.jobportal.pricing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lwd.jobportal.pricing.entity.Plan;
import com.lwd.jobportal.pricing.enums.PlanName;
import com.lwd.jobportal.pricing.enums.PlanType;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByTypeAndIsActiveTrue(PlanType type);
    List<Plan> findByTypeAndIsActiveTrueOrderByPriceAsc(PlanType type);
    Optional<Plan> findFirstByTypeAndName(PlanType type, PlanName name);



}
