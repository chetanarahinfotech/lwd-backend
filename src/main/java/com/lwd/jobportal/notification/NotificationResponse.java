package com.lwd.jobportal.notification;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private NotificationPriority priority;
    private String title;
    private String message;
    private Boolean isRead;
    private String actionUrl;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime readAt;
}