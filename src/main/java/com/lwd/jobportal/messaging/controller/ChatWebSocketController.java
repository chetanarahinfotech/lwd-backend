package com.lwd.jobportal.messaging.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;

import com.lwd.jobportal.messaging.dto.SendMessageDto;
import com.lwd.jobportal.messaging.service.MessagingService;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessagingService messagingService;

    // Maps to /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload @Valid SendMessageDto request, Principal principal) {
        if (principal == null) {
            log.error("Unauthenticated message sending attempt.");
            throw new IllegalArgumentException("User must be authenticated to send messages");
        }
        
        Long senderId = Long.valueOf(principal.getName());
        messagingService.sendMessage(senderId, request);
    }
}
