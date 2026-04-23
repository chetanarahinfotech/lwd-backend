package com.lwd.jobportal.notification;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    private Long userId;
    private NotificationType type;
    private NotificationPriority priority;
    private String title;
    private String message;
    private String actionUrl;
    private Long referenceId;
    private String referenceType;
}