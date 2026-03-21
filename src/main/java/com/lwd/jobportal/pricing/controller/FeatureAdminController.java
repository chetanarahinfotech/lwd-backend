package com.lwd.jobportal.pricing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.pricing.dto.FeatureRequest;
import com.lwd.jobportal.pricing.dto.FeatureResponse;
import com.lwd.jobportal.pricing.enums.PlanType;
import com.lwd.jobportal.pricing.service.FeatureAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plan/features")
@RequiredArgsConstructor
public class FeatureAdminController {

    private final FeatureAdminService featureAdminService;

    // ➕ Create a new feature
    @PostMapping
    public ResponseEntity<FeatureResponse> createFeature(@RequestBody FeatureRequest request) {
        FeatureResponse response = featureAdminService.createFeature(request);
        return ResponseEntity.ok(response);
    }

    // ✏️ Update an existing feature
    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponse> updateFeature(
            @PathVariable Long id,
            @RequestBody FeatureRequest request) {

        FeatureResponse response = featureAdminService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }

    // ❌ Delete a feature
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeature(@PathVariable Long id) {
        featureAdminService.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }

    // 📋 Get a single feature by ID
    @GetMapping("/{id}")
    public ResponseEntity<FeatureResponse> getFeature(@PathVariable Long id) {
        FeatureResponse response = featureAdminService.getFeature(id);
        return ResponseEntity.ok(response);
    }

    // 📋 List all features
    @GetMapping
    public ResponseEntity<List<FeatureResponse>> getAllFeatures() {
        List<FeatureResponse> features = featureAdminService.getAllFeatures();
        return ResponseEntity.ok(features);
    }

    // 📋 List features filtered by PlanType (JOB_SEEKER / RECRUITER)
    @GetMapping("/type/{planType}")
    public ResponseEntity<List<FeatureResponse>> getFeaturesByPlanType(@PathVariable PlanType planType) {
        List<FeatureResponse> features = featureAdminService.getFeaturesByPlanType(planType);
        return ResponseEntity.ok(features);
    }
}
