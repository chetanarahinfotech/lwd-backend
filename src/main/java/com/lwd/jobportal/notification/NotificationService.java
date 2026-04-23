package com.lwd.jobportal.notification;

import java.time.LocalDateTime;

import com.lwd.jobportal.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request, Long currentUserId) {

        Notification existing = null;

        if (shouldCheckDuplicates(request.getType())) {
            existing = findRecentDuplicate(request, currentUserId);
        }

        if (existing != null) {
            return mapToResponse(existing);
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .priority(request.getPriority())
                .title(request.getTitle())
                .message(request.getMessage())
                .actionUrl(request.getActionUrl())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        Notification saved = notificationRepository.save(notification);

        NotificationResponse response = mapToResponse(saved);

        sendRealtimeNotification(saved.getUserId(), response);
        sendUnreadCount(saved.getUserId());

        return response;
    }

    public Page<NotificationResponse> getMyNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return notificationRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        boolean changed = false;

        if (Boolean.FALSE.equals(notification.getIsRead())) {
            LocalDateTime now = LocalDateTime.now();
            notification.setIsRead(true);
            notification.setReadAt(now);
            notification.setUpdatedAt(now);
            notification.setUpdatedBy(userId);
            changed = true;
        }

        if (changed) {
            sendUnreadCount(userId);
        }

        return mapToResponse(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = notificationRepository.markAllAsRead(userId, now, userId);

        if (updatedCount > 0) {
            sendUnreadCount(userId);
        }

        return updatedCount;
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findByIdAndUserIdAndIsDeletedFalse(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        boolean wasUnread = Boolean.FALSE.equals(notification.getIsRead());

        int updated = notificationRepository.softDeleteByIdAndUserId(
                notificationId,
                userId,
                LocalDateTime.now(),
                userId
        );

        if (updated == 0) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }

        if (wasUnread) {
            sendUnreadCount(userId);
        }
    }

    private Notification findRecentDuplicate(CreateNotificationRequest request, Long currentUserId) {
        LocalDateTime threshold = resolveDuplicateThreshold(request.getType());

        return notificationRepository
                .findTopByUserIdAndTypeAndReferenceIdAndReferenceTypeAndCreatedByAndIsDeletedFalseAndCreatedAtAfterOrderByCreatedAtDesc(
                        request.getUserId(),
                        request.getType(),
                        request.getReferenceId(),
                        request.getReferenceType(),
                        currentUserId,
                        threshold
                )
                .orElse(null);
    }

    private boolean shouldCheckDuplicates(NotificationType type) {
        return switch (type) {
            case PROFILE_VIEWED, RESUME_VIEWED, APPLICATION_STATUS_CHANGED, RESUME_DOWNLOADED, NEW_APPLICATION_RECEIVED -> true;
            case NEW_MESSAGE -> false;
            default -> true;
        };
    }

    private LocalDateTime resolveDuplicateThreshold(NotificationType type) {
        LocalDateTime now = LocalDateTime.now();

        return switch (type) {
            case PROFILE_VIEWED -> now.minusHours(24);
            case RESUME_VIEWED -> now.minusHours(24);
            case RESUME_DOWNLOADED -> now.minusHours(24);
            case APPLICATION_STATUS_CHANGED -> now.minusMinutes(5);
            case NEW_APPLICATION_RECEIVED -> now.minusMinutes(5);
            case NEW_MESSAGE -> now.minusSeconds(30);
            default -> now.minusMinutes(10);
        };
    }

    private void sendRealtimeNotification(Long userId, NotificationResponse response) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                response
        );
    }
    
    private void sendUnreadCount(Long userId) {
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications/unread-count",
                unreadCount
        );
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .priority(notification.getPriority())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .actionUrl(notification.getActionUrl())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}