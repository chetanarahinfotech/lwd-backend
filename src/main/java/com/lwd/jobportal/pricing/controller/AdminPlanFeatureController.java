package com.lwd.jobportal.pricing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.pricing.dto.BulkPlanFeatureRequest;
import com.lwd.jobportal.pricing.dto.PlanFeatureRequest;
import com.lwd.jobportal.pricing.service.PlanFeatureAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/plan-features")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlanFeatureController {

    private final PlanFeatureAdminService service;

    /**
     * Upsert a single feature for a plan
     */
    @PostMapping("/{planId}")
    public ResponseEntity<String> upsertFeature(
            @PathVariable Long planId,
            @RequestBody PlanFeatureRequest request) {

        service.upsertFeature(planId, request);

        return ResponseEntity.ok("Feature updated successfully");
    }

    /**
     * Bulk upsert features for a plan
     */
    @PostMapping("/{planId}/bulk")
    public ResponseEntity<String> upsertFeaturesBulk(
            @PathVariable Long planId,
            @RequestBody BulkPlanFeatureRequest bulkRequest) {

        if (bulkRequest.getFeatures() == null || bulkRequest.getFeatures().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No features provided for bulk update");
        }

        service.upsertFeaturesBulk(planId, bulkRequest);

        return ResponseEntity.ok("Bulk feature update successful: " + bulkRequest.getFeatures().size() + " features processed");
    }

    /**
     * Optional: Fetch all features for a plan
     */
    @GetMapping("/{planId}")
    public ResponseEntity<List<PlanFeatureRequest>> getPlanFeatures(@PathVariable Long planId) {
        List<PlanFeatureRequest> features = service.getPlanFeaturesForPlan(planId);
        return ResponseEntity.ok(features);
    }
    
    @GetMapping("/{planId}/all")
    public ResponseEntity<List<PlanFeatureRequest>> getAllFeaturesByPlanType(
            @PathVariable Long planId) {

        return ResponseEntity.ok(service.getAllFeaturesByPlanType(planId));
    }

}
