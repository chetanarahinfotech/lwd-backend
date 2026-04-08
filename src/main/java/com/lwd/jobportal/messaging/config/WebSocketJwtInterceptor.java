package com.lwd.jobportal.messaging.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.lwd.jobportal.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Long userId = jwtUtil.extractUserId(token);
                    String email = jwtUtil.extractUsername(token);
                    
                    if (userId != null) {
                        Principal principal = new StompPrincipal(userId.toString(), email);
                        accessor.setUser(principal);
                        log.info("WebSocket user configured for user ID: {}", userId);
                    }
                } catch (Exception e) {
                    log.error("WebSocket Authentication failed: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid JWT token for STOMP connection", e);
                }
            } else {
                log.error("No valid Authorization header provided in STOMP CONNECT");
                throw new IllegalArgumentException("No Authorization header provided");
            }
        }
        return message;
    }
}
