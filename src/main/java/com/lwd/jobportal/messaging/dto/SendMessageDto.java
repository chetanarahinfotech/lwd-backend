package com.lwd.jobportal.messaging.dto;

import com.lwd.jobportal.messaging.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageDto {

    @NotBlank(message = "Client message ID must not be blank for duplicate tracking")
    private String clientMessageId;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 1000, message = "Message content must not exceed 1000 characters")
    private String content;

    private MessageType type = MessageType.TEXT; // default to text
}
