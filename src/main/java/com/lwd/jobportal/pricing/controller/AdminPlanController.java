package com.lwd.jobportal.pricing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.pricing.dto.PlanRequest;
import com.lwd.jobportal.pricing.dto.PlanResponse;
import com.lwd.jobportal.pricing.service.PlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 🔥 SECURITY
public class AdminPlanController {

    private final PlanService planService;

    // ✅ Create Plan
    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(
            @RequestBody PlanRequest request) {

        return ResponseEntity.ok(
                planService.createPlan(request)
        );
    }

    // 🔄 Update Plan
    @PutMapping("/{planId}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable Long planId,
            @RequestBody PlanRequest request) {

        return ResponseEntity.ok(
                planService.updatePlan(planId, request)
        );
    }

    // 📊 Get All Plans
    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {

        return ResponseEntity.ok(
                planService.getAllPlans()
        );
    }

    // 🔥 Enable/Disable Plan
    @PatchMapping("/{planId}/toggle")
    public ResponseEntity<String> togglePlan(
            @PathVariable Long planId,
            @RequestParam boolean active) {

        planService.togglePlan(planId, active);

        return ResponseEntity.ok("Plan updated successfully");
    }
}
