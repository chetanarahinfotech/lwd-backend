package com.lwd.jobportal.pricing;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeatureAdminService {

    private final FeatureRepository featureRepository;

    @CacheEvict(
        value = {"candidatePlans", "recruiterPlans", "CompanyAdminPlans", "allPlans", "planFeatures", "planFeature"},
        allEntries = true
    )
    @Transactional
    public FeatureResponse createFeature(FeatureRequest request) {
        String code = normalizeCode(request.getCode());

        if (featureRepository.existsByCodeAndPlanType(code, request.getPlanType())) {
            throw new InvalidOperationException(
                "Feature code already exists for plan type: " + code + " - " + request.getPlanType()
            );
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
        value = {"candidatePlans", "recruiterPlans", "CompanyAdminPlans", "allPlans", "planFeatures", "planFeature"},
        allEntries = true
    )
    @Transactional
    public FeatureResponse updateFeature(Long id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));

        String code = normalizeCode(request.getCode());

        if ((!feature.getCode().equals(code) || feature.getPlanType() != request.getPlanType())
                && featureRepository.existsByCodeAndPlanType(code, request.getPlanType())) {
            throw new InvalidOperationException(
                "Feature code already exists for plan type: " + code + " - " + request.getPlanType()
            );
        }

        feature.setCode(code);
        feature.setPlanType(request.getPlanType());
        feature.setDescription(request.getDescription());

        feature = featureRepository.save(feature);

        return mapToResponse(feature, null);
    }

    @CacheEvict(
        value = {"candidatePlans", "recruiterPlans", "CompanyAdminPlans", "allPlans", "planFeatures", "planFeature"},
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