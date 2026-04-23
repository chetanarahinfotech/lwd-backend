package com.lwd.jobportal.messaging;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

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
