package com.lwd.jobportal.pricing.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.pricing.dto.FeatureRequest;
import com.lwd.jobportal.pricing.dto.FeatureResponse;
import com.lwd.jobportal.pricing.entity.Feature;
import com.lwd.jobportal.pricing.entity.PlanFeature;
import com.lwd.jobportal.pricing.enums.PlanType;
import com.lwd.jobportal.pricing.repository.FeatureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeatureAdminService {

    private final FeatureRepository featureRepository;

    @CacheEvict(
    	    value = {"candidatePlans", "recruiterPlans", "allPlans", "planFeatures", "planFeature"},
    	    allEntries = true
    	)
    @Transactional
    public FeatureResponse createFeature(FeatureRequest request) {
        String code = normalizeCode(request.getCode());

        if (featureRepository.existsByCode(code)) {
            throw new InvalidOperationException("Feature code already exists: " + code);
        }

        Feature feature = Feature.builder()
                .code(code)
                .planType(request.getPlanType())
                .description(request.getDescription())
                .build();

        feature = featureRepository.save(feature);

        return mapToResponse(feature, null);
    }

    @CacheEvict(
    	    value = {"candidatePlans", "recruiterPlans", "allPlans", "planFeatures", "planFeature"},
    	    allEntries = true
    	)
    @Transactional
    public FeatureResponse updateFeature(Long id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));

        feature.setPlanType(request.getPlanType());
        feature.setDescription(request.getDescription());

        feature = featureRepository.save(feature);

        return mapToResponse(feature, null);
    }

    @CacheEvict(
    	    value = {"candidatePlans", "recruiterPlans", "allPlans", "planFeatures", "planFeature"},
    	    allEntries = true
    	)
    @Transactional
    public void deleteFeature(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));

        featureRepository.delete(feature);
    }

    public FeatureResponse getFeature(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));
        return mapToResponse(feature, null);
    }

    public List<FeatureResponse> getAllFeatures() {
        return featureRepository.findAll().stream()
                .map(feature -> mapToResponse(feature, null))
                .toList();
    }

    public List<FeatureResponse> getFeaturesByPlanType(PlanType planType) {
        return featureRepository.findByPlanType(planType).stream()
                .map(feature -> mapToResponse(feature, null))
                .toList();
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

    private FeatureResponse mapToResponse(Feature feature, PlanFeature planFeature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .featureCode(feature.getCode())
                .planType(feature.getPlanType())
                .description(feature.getDescription())
                .enabled(planFeature != null && Boolean.TRUE.equals(planFeature.getEnabled()))
                .limitValue(planFeature != null ? planFeature.getLimitValue() : null)
                .limitType(planFeature != null ? planFeature.getLimitType() : null)
                .build();
    }
}