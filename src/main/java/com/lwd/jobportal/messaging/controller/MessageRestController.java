package com.lwd.jobportal.messaging.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lwd.jobportal.messaging.dto.ChatMessageResponseDto;
import com.lwd.jobportal.messaging.dto.ConversationSummaryDto;
import com.lwd.jobportal.messaging.dto.DeleteMessagesRequest;
import com.lwd.jobportal.messaging.service.MessagingService;

import com.lwd.jobportal.security.JwtUtil;
import com.lwd.jobportal.security.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class MessageRestController {

    private final MessagingService messagingService;
    private final JwtUtil jwtUtil;

    // Helper to get userId from current request token
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new IllegalArgumentException("Unauthorized");
    }
    
    @PostMapping("/conversations/direct/{otherUserId}")
    public ResponseEntity<ConversationSummaryDto> getOrCreateDirectConversation(
            HttpServletRequest request,
            @PathVariable Long otherUserId) {

        Long currentUserId = getCurrentUserId(request);
        ConversationSummaryDto conversation =
                messagingService.getOrCreateConversationWithUser(currentUserId, otherUserId);

        return ResponseEntity.ok(conversation);
    }

    // Inbox Endpoint: Get all active conversations for the user
    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationSummaryDto>> getInboxConversations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getCurrentUserId(request);
        Page<ConversationSummaryDto> inbox = messagingService.getInbox(userId, page, size);
        return ResponseEntity.ok(inbox);
    }

    // Message History Endpoint: Cursor paginated
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getConversationHistory(
            HttpServletRequest request,
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int limit) {
        
        Long userId = getCurrentUserId(request);
        List<ChatMessageResponseDto> messages = messagingService.getMessagesByCursor(userId, conversationId, cursorId, limit);
        return ResponseEntity.ok(messages);
    }

    // Mark conversation read
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            HttpServletRequest request,
            @PathVariable Long conversationId) {
        
        Long userId = getCurrentUserId(request);
        messagingService.markConversationAsRead(userId, conversationId);
        return ResponseEntity.ok().build();
    }
    
    
    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long conversationId,
                                           @PathVariable Long messageId) {
        Long currentUserId = SecurityUtils.getUserId();

        messagingService.softDeleteMessage(currentUserId, conversationId, messageId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message deleted successfully"
        ));
    }

    @DeleteMapping("/messages/bulk")
    public ResponseEntity<?> deleteSelectedMessages(@RequestBody DeleteMessagesRequest request) {
        Long currentUserId = SecurityUtils.getUserId();

        int deletedCount = messagingService.softDeleteSelectedMessages(currentUserId, request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Selected messages deleted successfully",
                "deletedCount", deletedCount
        ));
    }
}
