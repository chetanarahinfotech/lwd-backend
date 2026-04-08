package com.lwd.jobportal.messaging.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeleteMessagesRequest {

    @NotNull
    private Long conversationId;

    @NotEmpty
    private List<Long> messageIds;
}