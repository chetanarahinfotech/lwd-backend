package com.lwd.jobportal.pricing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.pricing.entity.Feature;
import com.lwd.jobportal.pricing.enums.PlanType;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {

	Optional<Feature> findByCode(String code);


    // ✅ Optional: check existence (useful for validation)
    boolean existsByCode(String code);
    
    List<Feature> findByCodeIn(List<String> codes);


	List<Feature> findByPlanType(PlanType planType);

}
