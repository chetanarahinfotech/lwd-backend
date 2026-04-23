package com.lwd.jobportal.pricing;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
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
    
    /**
     * 🏢 Recruiter Admin Plans
     */
    @GetMapping("/company-admin")
    public ResponseEntity<List<RecruiterPlanResponse>> getCompanyAdminPlans() {

        return ResponseEntity.ok(
                planService.getCompanyAdminPlans()
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
        
        if (SecurityUtils.hasRole(Role.COMPANY_ADMIN)) {
            return ResponseEntity.ok(planService.getCompanyAdminPlans());
        }

        throw new RuntimeException("Unauthorized role");
    }
}
