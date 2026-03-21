package com.lwd.jobportal.pricing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.pricing.dto.SubscriptionResponse;
import com.lwd.jobportal.pricing.service.SubscriptionService;
import com.lwd.jobportal.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 🔥 Subscribe / Upgrade / Downgrade Plan
     */
    @PostMapping("/subscribe/{planId}")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @PathVariable Long planId) {

        Long userId = SecurityUtils.getUserId();

        SubscriptionResponse response =
                subscriptionService.subscribe(userId, planId);

        return ResponseEntity.ok(response);
    }

    /**
     * 📊 Get Current Active Subscription
     */
    @GetMapping("/current")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription() {

        Long userId = SecurityUtils.getUserId();

        var sub = subscriptionService.getActiveSubscription(userId);

        if (sub == null) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(
                SubscriptionResponse.builder()
                        .planName(sub.getPlan().getName().name())
                        .planType(sub.getPlan().getType().name())
                        .price(sub.getPlan().getPrice())
                        .startDate(sub.getStartDate())
                        .endDate(sub.getEndDate())
                        .status(sub.getStatus().name())
                        .build()
        );
    }

    /**
     * ❌ Cancel Subscription (Optional Feature)
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelSubscription() {

        Long userId = SecurityUtils.getUserId();

        subscriptionService.cancelSubscription(userId);

        return ResponseEntity.ok("Subscription cancelled successfully");
    }

}
