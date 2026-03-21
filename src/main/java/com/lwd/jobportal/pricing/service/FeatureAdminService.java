package com.lwd.jobportal.pricing.service;

import java.util.List;
import java.util.stream.Collectors;

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

    // ➕ Create Feature
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

    // ✏️ Update Feature (code immutable)
    @Transactional
    public FeatureResponse updateFeature(Long id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));

        feature.setPlanType(request.getPlanType());
        feature.setDescription(request.getDescription());

        feature = featureRepository.save(feature);

        return mapToResponse(feature, null);
    }

    // ❌ Delete Feature
    @Transactional
    public void deleteFeature(Long id) {
        if (!featureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Feature not found with id: " + id);
        }
        featureRepository.deleteById(id);
    }

    // 📋 Get single feature
    public FeatureResponse getFeature(Long id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));
        return mapToResponse(feature, null);
    }

    // 📋 List all features
    public List<FeatureResponse> getAllFeatures() {
        return featureRepository.findAll().stream()
                .map(f -> mapToResponse(f, null))
                .collect(Collectors.toList());
    }

    // 📋 List features by PlanType
    public List<FeatureResponse> getFeaturesByPlanType(PlanType planType) {
        return featureRepository.findAll().stream()
                .filter(f -> f.getPlanType() == planType)
                .map(f -> mapToResponse(f, null))
                .collect(Collectors.toList());
    }

    // 🔹 Normalize code input
    private String normalizeCode(String code) {
        return code.toUpperCase().replaceAll("[^A-Z_]", "_");
    }

    // 🔹 Map feature + optional planFeature to FeatureResponse
    private FeatureResponse mapToResponse(Feature feature, PlanFeature planFeature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .featureCode(feature.getCode())
                .planType(feature.getPlanType())
                .description(feature.getDescription())
                .enabled(planFeature != null && planFeature.getEnabled())
                .limitValue(planFeature != null ? planFeature.getLimitValue() : null)
                .limitType(planFeature != null ? planFeature.getLimitType() : null)
                .build();
    }
}
