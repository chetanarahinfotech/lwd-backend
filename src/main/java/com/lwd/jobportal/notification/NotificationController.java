package com.lwd.jobportal.notification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.util.SecurityUtils;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(notificationService.getMyNotifications(userId, page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, userId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead() {
        Long userId = SecurityUtils.getUserId();
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("Marked " + updatedCount + " notifications as read");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
        Long userId = SecurityUtils.getUserId();
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok("Notification deleted successfully");
    }
}