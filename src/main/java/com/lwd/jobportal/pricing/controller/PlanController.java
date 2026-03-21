package com.lwd.jobportal.pricing.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.pricing.dto.CandidatePlanResponse;
import com.lwd.jobportal.pricing.dto.RecruiterPlanResponse;
import com.lwd.jobportal.pricing.service.PlanService;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('RECRUITER','JOB_SEEKER','ADMIN')")
public class PlanController {

    private final PlanService planService;

    /**
     * 👤 Candidate Plans
     */
    @GetMapping("/candidate")
    public ResponseEntity<List<CandidatePlanResponse>> getCandidatePlans() {

        return ResponseEntity.ok(
                planService.getCandidatePlans()
        );
    }

    /**
     * 🏢 Recruiter Plans
     */
    @GetMapping("/recruiter")
    public ResponseEntity<List<RecruiterPlanResponse>> getRecruiterPlans() {

        return ResponseEntity.ok(
                planService.getRecruiterPlans()
        );
    }
    
    
    @GetMapping
    public ResponseEntity<?> getPlansByRole() {

        // 🔥 ADMIN → see ALL
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return ResponseEntity.ok(planService.getAllPlans());
        }

        // 👤 Candidate
        if (SecurityUtils.hasRole(Role.JOB_SEEKER)) {
            return ResponseEntity.ok(planService.getCandidatePlans());
        }

        // 🏢 Recruiter
        if (SecurityUtils.hasRole(Role.RECRUITER)) {
            return ResponseEntity.ok(planService.getRecruiterPlans());
        }

        throw new RuntimeException("Unauthorized role");
    }
}
