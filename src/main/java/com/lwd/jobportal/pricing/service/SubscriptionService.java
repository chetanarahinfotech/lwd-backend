package com.lwd.jobportal.pricing.service;

import java.time.LocalDateTime;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.exception.ResourceNotFoundException;
import com.lwd.jobportal.exception.SubscriptionNotFoundException;
import com.lwd.jobportal.pricing.dto.SubscriptionResponse;
import com.lwd.jobportal.pricing.entity.Plan;
import com.lwd.jobportal.pricing.entity.SubscriptionHistory;
import com.lwd.jobportal.pricing.entity.UserSubscription;
import com.lwd.jobportal.pricing.enums.ActionType;
import com.lwd.jobportal.pricing.enums.SubscriptionStatus;
import com.lwd.jobportal.pricing.repository.PlanRepository;
import com.lwd.jobportal.pricing.repository.SubscriptionHistoryRepository;
import com.lwd.jobportal.pricing.repository.UserSubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PlanRepository planRepository;
    private final SubscriptionHistoryRepository historyRepository;

    @Cacheable(value = "userPlan", key = "#userId")
    public UserSubscription getActiveSubscription(Long userId) {

        UserSubscription sub = userSubscriptionRepository
                .findActiveWithPlan(userId)
                .orElse(null);

        if (sub == null) {
            return null; // handled at higher layer
        }

        // 🔥 REAL-TIME EXPIRY CHECK
        if (sub.getEndDate().isBefore(LocalDateTime.now())) {

            sub.setStatus(SubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(sub);

            // ❌ DON'T return expired plan
            return null;
        }

        return sub;
    }



    // 🔥 Subscribe / Upgrade / Downgrade
    @Transactional
    @CacheEvict(value = "userPlan", key = "#userId") // ✅ IMPORTANT
    public SubscriptionResponse subscribe(Long userId, Long planId) {

        Plan newPlan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        // 🔍 Get current active subscription
        UserSubscription existing = userSubscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElse(null);

        ActionType actionType = null;

        if (existing != null) {

            Plan oldPlan = existing.getPlan();

            // ❌ Prevent same plan re-subscribe
            if (oldPlan.getId().equals(planId)) {
                throw new InvalidOperationException("You are already subscribed to this plan");
            }

            // 🔥 Detect action type
            actionType = determineAction(oldPlan, newPlan);

            // 🔁 Expire old subscription
            existing.setStatus(SubscriptionStatus.EXPIRED);
            existing.setEndDate(LocalDateTime.now());
            userSubscriptionRepository.save(existing);

            // 📜 Save history
            saveHistory(
                    userId,
                    oldPlan.getId(),
                    newPlan.getId(),
                    actionType,
                    newPlan.getPrice()
            );
        } else {
            // 🆕 First time subscription
            actionType = ActionType.RENEW;

            saveHistory(
                    userId,
                    null,
                    newPlan.getId(),
                    actionType,
                    newPlan.getPrice()
            );
        }

        // ✅ Create new subscription
        UserSubscription newSub = UserSubscription.builder()
                .userId(userId)
                .plan(newPlan)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(newPlan.getDurationDays()))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        userSubscriptionRepository.save(newSub);

        return mapToResponse(newSub);
    }
    
    
    @Transactional
    @CacheEvict(value = "userPlan", key = "#userId")
    public void cancelSubscription(Long userId) {

    	UserSubscription sub = userSubscriptionRepository
    	        .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
    	        .orElseThrow(() -> new SubscriptionNotFoundException(
    	                "No active subscription found for user"
    	        ));



        sub.setStatus(SubscriptionStatus.EXPIRED);
        sub.setEndDate(LocalDateTime.now());

        userSubscriptionRepository.save(sub);
    }


    // 🧠 Determine action type
    private ActionType determineAction(Plan oldPlan, Plan newPlan) {

        if (newPlan.getPrice() > oldPlan.getPrice()) {
            return ActionType.UPGRADE;
        }

        if (newPlan.getPrice() < oldPlan.getPrice()) {
            return ActionType.DOWNGRADE;
        }

        return ActionType.RENEW;
    }

    // 📜 Save history
    private void saveHistory(Long userId,
                             Long oldPlanId,
                             Long newPlanId,
                             ActionType action,
                             Double amount) {

        SubscriptionHistory history = SubscriptionHistory.builder()
                .userId(userId)
                .oldPlanId(oldPlanId)
                .newPlanId(newPlanId)
                .action(action)
                .amountPaid(amount)
                .build();

        historyRepository.save(history);
    }

    // 🔄 Map response cleanly
    private SubscriptionResponse mapToResponse(UserSubscription sub) {

        Plan plan = sub.getPlan();

        return SubscriptionResponse.builder()
                .planName(plan.getName().name())
                .planType(plan.getType().name())
                .price(plan.getPrice())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .status(sub.getStatus().name())
                .build();
    }
}
