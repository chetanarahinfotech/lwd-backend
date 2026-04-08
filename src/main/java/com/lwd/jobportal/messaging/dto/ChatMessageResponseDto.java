package com.lwd.jobportal.messaging.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import com.lwd.jobportal.messaging.entity.MessageStatus;
import com.lwd.jobportal.messaging.entity.MessageType;

@Data
@Builder
public class ChatMessageResponseDto {
    private Long id;
    private String clientMessageId;
    private Long senderId;
    private Long conversationId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private LocalDateTime createdAt;
}
