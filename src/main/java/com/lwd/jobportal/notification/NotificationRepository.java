package com.lwd.jobportal.notification;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    long countByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);

    Optional<Notification> findTopByUserIdAndTypeAndReferenceIdAndReferenceTypeAndCreatedByAndIsDeletedFalseAndCreatedAtAfterOrderByCreatedAtDesc(
            Long userId,
            NotificationType type,
            Long referenceId,
            String referenceType,
            Long createdBy,
            LocalDateTime createdAt
    );

    @Modifying
    @Transactional
    @Query("""
           update Notification n
           set n.isRead = true,
               n.readAt = :readAt,
               n.updatedAt = :readAt,
               n.updatedBy = :updatedBy
           where n.userId = :userId
             and n.isDeleted = false
             and n.isRead = false
           """)
    int markAllAsRead(
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt,
            @Param("updatedBy") Long updatedBy
    );

    @Modifying
    @Transactional
    @Query("""
           update Notification n
           set n.isDeleted = true,
               n.deletedAt = :deletedAt,
               n.deletedBy = :deletedBy,
               n.updatedAt = :deletedAt,
               n.updatedBy = :deletedBy
           where n.id = :notificationId
             and n.userId = :userId
             and n.isDeleted = false
           """)
    int softDeleteByIdAndUserId(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId,
            @Param("deletedAt") LocalDateTime deletedAt,
            @Param("deletedBy") Long deletedBy
    );
}