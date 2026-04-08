package com.lwd.jobportal.messaging.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketRateLimitInterceptor implements ChannelInterceptor {

    private final ConcurrentHashMap<String, Queue<Long>> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES_PER_SECOND = 5;
    private static final long TIME_WINDOW_MS = 1000;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Only rate limit actual message sending
        if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
            Principal user = accessor.getUser();
            if (user == null || user.getName() == null) {
                log.warn("Unauthenticated sending attempt blocked by Rate Limiter.");
                throw new IllegalArgumentException("User principal not found");
            }

            String userId = user.getName();
            long now = System.currentTimeMillis();

            Queue<Long> timestamps = requestCounts.computeIfAbsent(userId, k -> new LinkedList<>());
            
            synchronized (timestamps) {
                // Remove out-of-window timestamps
                while (!timestamps.isEmpty() && now - timestamps.peek() > TIME_WINDOW_MS) {
                    timestamps.poll();
                }

                if (timestamps.size() >= MAX_MESSAGES_PER_SECOND) {
                    log.warn("Rate limit exceeded for user: {}", userId);
                    throw new IllegalArgumentException("Rate limit exceeded! Please slow down.");
                }
                
                timestamps.add(now);
            }
        }
        return message;
    }
}
