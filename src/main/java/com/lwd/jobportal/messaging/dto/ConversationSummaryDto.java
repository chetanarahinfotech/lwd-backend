package com.lwd.jobportal.messaging.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

import com.lwd.jobportal.messaging.entity.MessageStatus;

@Data
@Builder
public class ConversationSummaryDto {
    private Long conversationId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfileImageUrl;

    private String lastMessageText;
    private LocalDateTime lastMessageAt;
    private long unreadCount;

    // 🔥 ADD THESE
    private MessageStatus lastMessageStatus;
    private Long lastMessageSenderId;
    
    // Active status
    private Boolean isActive;
    private LocalDateTime lastActiveAt;
}