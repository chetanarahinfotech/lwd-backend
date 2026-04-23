package com.lwd.jobportal.pricing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {

	Optional<Feature> findByCode(String code);


    // ✅ Optional: check existence (useful for validation)
    boolean existsByCode(String code);
    
    boolean existsByCodeAndPlanType(String code, PlanType planType);
    
    List<Feature> findByCodeIn(List<String> codes);


	List<Feature> findByPlanType(PlanType planType);
	
	List<Feature> findByPlanTypeIn(List<PlanType> planTypes);
	
	List<Feature> findByCodeInAndPlanTypeIn(List<String> codes, List<PlanType> planTypes);
	
	List<Feature> findByCodeAndPlanTypeIn(String code, List<PlanType> planTypes);
	
	Optional<Feature> findByCodeAndPlanType(String code, PlanType planType);


}
