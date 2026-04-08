package com.lwd.jobportal.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.service.UserActivityService;
import com.lwd.jobportal.messaging.dto.ChatMessageResponseDto;
import com.lwd.jobportal.messaging.dto.SendMessageDto;
import com.lwd.jobportal.messaging.entity.Conversation;
import com.lwd.jobportal.messaging.entity.ConversationParticipant;
import com.lwd.jobportal.messaging.entity.Message;
import com.lwd.jobportal.messaging.entity.MessageAudit;
import com.lwd.jobportal.messaging.entity.MessageStatus;
import com.lwd.jobportal.messaging.repository.ConversationParticipantRepository;
import com.lwd.jobportal.messaging.repository.ConversationRepository;
import com.lwd.jobportal.messaging.repository.MessageAuditRepository;
import com.lwd.jobportal.messaging.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.messaging.dto.ConversationSummaryDto;
import com.lwd.jobportal.messaging.dto.DeleteMessagesRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageAuditRepository auditRepository;
    private final UserRepository userRepository;
    private final UserActivityService userActivityService;
    
    
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponseDto sendMessage(Long senderId, SendMessageDto request) {
        // Idempotency / Duplicate Check
        if (messageRepository.existsByClientMessageIdAndSenderId(request.getClientMessageId(), senderId)) {
            log.warn("Duplicate message avoided. clientMessageId: {}, senderId: {}", request.getClientMessageId(), senderId);
            throw new IllegalArgumentException("Duplicate message detected.");
        }

        // Validate Users
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sender"));
        
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipient"));

        if (!sender.getIsActive() || !recipient.getIsActive()) { // from User.java isActive mapped to getIsActive
            throw new IllegalArgumentException("Both users must be active to communicate.");
        }

        if (senderId.equals(request.getRecipientId())) {
            throw new IllegalArgumentException("Cannot send message to yourself.");
        }

        // Requirement: Recruiter <-> Job Seeker validation
        boolean isSenderRecruiter = sender.getRole() == Role.RECRUITER || sender.getRole() == Role.RECRUITER_ADMIN;
        boolean isSenderSeeker = sender.getRole() == Role.JOB_SEEKER;
        boolean isRecipientRecruiter = recipient.getRole() == Role.RECRUITER || recipient.getRole() == Role.RECRUITER_ADMIN;
        boolean isRecipientSeeker = recipient.getRole() == Role.JOB_SEEKER;

        if (!((isSenderRecruiter && isRecipientSeeker) || (isSenderSeeker && isRecipientRecruiter))) {
            throw new IllegalArgumentException("Messaging is only permitted between Recruiters and Job Seekers.");
        }

        // Fetch or Create Conversation Context (unique by user pair constraint)
        Conversation conversation = conversationRepository.findConversationByUsers(senderId, request.getRecipientId())
                .orElseGet(() -> createConversationContext(sender, recipient));

        // Create Message
        Message message = Message.builder()
                .clientMessageId(request.getClientMessageId())
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .type(request.getType())
                .status(MessageStatus.SENT)
                .isSoftDeleted(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Update Conversation Metadata
        conversation.setLastMessageText(savedMessage.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation); // Though @Transactional usually handles dirty checking, explicitly calling save is safer for detached contexts.

        // Append Audit (CREATE only)
        MessageAudit audit = MessageAudit.builder()
                .messageId(savedMessage.getId())
                .userId(senderId)
                .actionType("CREATED")
                .build();
        auditRepository.save(audit);

        // Convert to DTO
        ChatMessageResponseDto response = ChatMessageResponseDto.builder()
                .id(savedMessage.getId())
                .clientMessageId(savedMessage.getClientMessageId())
                .senderId(senderId)
                .conversationId(conversation.getId())
                .content(savedMessage.getContent())
                .type(savedMessage.getType())
                .status(savedMessage.getStatus())
                .createdAt(savedMessage.getCreatedAt())
                .build();

     // Send real-time message to recipient
        messagingTemplate.convertAndSendToUser(
                request.getRecipientId().toString(),
                "/queue/messages",
                response
        );

        // Send ack / echo back to sender so frontend can update optimistic message
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/acks",
                response
        );

        return response;
    }

    private Conversation createConversationContext(User sender, User recipient) {
        User user1 = sender.getId() < recipient.getId() ? sender : recipient;
        User user2 = sender.getId() < recipient.getId() ? recipient : sender;

        Conversation convo = Conversation.builder()
                .user1(user1)
                .user2(user2)
                .build();

        Conversation savedConvo = conversationRepository.save(convo);

        ConversationParticipant participant1 = ConversationParticipant.builder()
                .conversation(savedConvo)
                .user(user1)
                .lastReadMessageId(0L)
                .build();

        ConversationParticipant participant2 = ConversationParticipant.builder()
                .conversation(savedConvo)
                .user(user2)
                .lastReadMessageId(0L)
                .build();

        participantRepository.save(participant1);
        participantRepository.save(participant2);

        return savedConvo;
    }

    @Transactional
    public void markConversationAsRead(Long userId, Long conversationId) {
        ConversationParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a participant in this conversation."));

        Long latestMessageId = messageRepository.findFirstByConversationIdOrderByIdDesc(conversationId)
                .map(Message::getId)
                .orElse(null);

        if (latestMessageId != null) {
            participant.setLastReadMessageId(latestMessageId);
            participant.setLastReadAt(LocalDateTime.now());
            participantRepository.save(participant);

            messageRepository.markMessagesAsRead(conversationId, userId);
        }
    }

    @Transactional
    public List<ChatMessageResponseDto> getMessagesByCursor(
            Long userId,
            Long conversationId,
            Long cursorId,
            int limit
    ) {
        participantRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Unauthorized access to conversation history."));

        messageRepository.markMessagesAsDelivered(conversationId, userId);
        messageRepository.flush(); // if using JpaRepository

        List<Message> messages = messageRepository.findMessagesByConversationIdBeforeCursor(
                conversationId,
                cursorId,
                PageRequest.of(0, limit)
        );

        return messages.stream()
                .map(m -> ChatMessageResponseDto.builder()
                        .id(m.getId())
                        .clientMessageId(m.getClientMessageId())
                        .senderId(m.getSender().getId())
                        .conversationId(m.getConversation().getId())
                        .content(m.getContent())
                        .type(m.getType())
                        .status(m.getStatus())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    
   @Transactional
	public ConversationSummaryDto getOrCreateConversationWithUser(Long currentUserId, Long otherUserId) {
	
	    if (currentUserId == null) {
	        throw new IllegalArgumentException("Unauthorized");
	    }
	
	    if (otherUserId == null) {
	        throw new IllegalArgumentException("Target user id is required");
	    }
	
	    if (currentUserId.equals(otherUserId)) {
	        throw new IllegalArgumentException("Cannot create conversation with yourself");
	    }
	
	    User currentUser = userRepository.findById(currentUserId)
	            .orElseThrow(() -> new IllegalArgumentException("Invalid current user"));
	
	    User otherUser = userRepository.findById(otherUserId)
	            .orElseThrow(() -> new IllegalArgumentException("Invalid target user"));
	
	    // ✅ reuse your existing validation logic
	    validateMessagingAllowed(currentUser, otherUser);
	
	    // ✅ reuse your existing conversation logic
	    Conversation conversation = conversationRepository
	            .findConversationByUsers(currentUserId, otherUserId)
	            .orElseGet(() -> createConversationContext(currentUser, otherUser));
	
	    // ✅ get participant
	    ConversationParticipant participant = participantRepository
	            .findByConversationIdAndUserId(conversation.getId(), currentUserId)
	            .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
	
	    long unreadCount = messageRepository.countUnreadMessages(
	            conversation.getId(),
	            participant.getLastReadMessageId() != null
	                    ? participant.getLastReadMessageId()
	                    : 0L
	    );
	
	    User other = conversation.getUser1().getId().equals(currentUserId)
	            ? conversation.getUser2()
	            : conversation.getUser1();
	
	    return ConversationSummaryDto.builder()
	            .conversationId(conversation.getId())
	            .otherUserId(other.getId())
	            .otherUserName(other.getName())
	            .otherUserProfileImageUrl(other.getProfileImageUrl())
	            .lastMessageText(conversation.getLastMessageText())
	            .lastMessageAt(conversation.getLastMessageAt())
	            .unreadCount(unreadCount)
	            .build();
	}

   
   private void validateMessagingAllowed(User sender, User recipient) {
	    boolean isSenderRecruiter =
	            sender.getRole() == Role.RECRUITER || sender.getRole() == Role.RECRUITER_ADMIN;
	    boolean isSenderSeeker = sender.getRole() == Role.JOB_SEEKER;

	    boolean isRecipientRecruiter =
	            recipient.getRole() == Role.RECRUITER || recipient.getRole() == Role.RECRUITER_ADMIN;
	    boolean isRecipientSeeker = recipient.getRole() == Role.JOB_SEEKER;

	    if (!((isSenderRecruiter && isRecipientSeeker) ||
	          (isSenderSeeker && isRecipientRecruiter))) {
	        throw new IllegalArgumentException("Messaging is only permitted between Recruiters and Job Seekers.");
	    }
	}
   @Transactional(readOnly = true)
   public Page<ConversationSummaryDto> getInbox(Long userId, int page, int size) {
       Page<ConversationParticipant> participants =
               participantRepository.findByUserIdOrderByConversationLastMessageAtDesc(
                       userId,
                       PageRequest.of(page, size)
               );

       List<ConversationParticipant> participantList = participants.getContent();

       if (participantList.isEmpty()) {
           return participants.map(p -> ConversationSummaryDto.builder().build());
       }

       // 1. Collect conversation ids
       List<Long> conversationIds = participantList.stream()
               .map(p -> p.getConversation().getId())
               .toList();

       // 2. Collect other user ids
       List<Long> otherUserIds = participantList.stream()
               .map(p -> {
                   Conversation conv = p.getConversation();
                   return conv.getUser1().getId().equals(userId)
                           ? conv.getUser2().getId()
                           : conv.getUser1().getId();
               })
               .distinct()
               .toList();

       // 3. Bulk fetch active status
       Map<Long, Boolean> activeMap = userActivityService.getActiveUsers(otherUserIds);

       // 4. Bulk fetch last messages for all conversations
       Map<Long, Message> lastMessageMap = messageRepository
               .findLastMessagesByConversationIds(conversationIds)
               .stream()
               .collect(Collectors.toMap(
                       message -> message.getConversation().getId(),
                       Function.identity()
               ));

       // 5. Bulk fetch unread counts
       Map<Long, Long> unreadCountMap = messageRepository
               .countUnreadMessagesForUserConversations(userId, conversationIds)
               .stream()
               .collect(Collectors.toMap(
                       row -> (Long) row[0],   // conversationId
                       row -> (Long) row[1]    // unreadCount
               ));

       List<ConversationSummaryDto> dtoList = participantList.stream()
               .map(p -> {
                   Conversation conv = p.getConversation();

                   User otherUser = conv.getUser1().getId().equals(userId)
                           ? conv.getUser2()
                           : conv.getUser1();

                   Message lastMessage = lastMessageMap.get(conv.getId());
                   boolean isActive = activeMap.getOrDefault(otherUser.getId(), false);
                   long unreadCount = unreadCountMap.getOrDefault(conv.getId(), 0L);

                   return ConversationSummaryDto.builder()
                           .conversationId(conv.getId())
                           .otherUserId(otherUser.getId())
                           .otherUserName(otherUser.getName())
                           .otherUserProfileImageUrl(otherUser.getProfileImageUrl())
                           .lastMessageText(lastMessage != null ? lastMessage.getContent() : null)
                           .lastMessageAt(lastMessage != null
                                   ? lastMessage.getCreatedAt()
                                   : conv.getLastMessageAt())
                           .unreadCount(unreadCount)
                           .lastMessageStatus(lastMessage != null ? lastMessage.getStatus() : null)
                           .lastMessageSenderId(
                                   lastMessage != null && lastMessage.getSender() != null
                                           ? lastMessage.getSender().getId()
                                           : null
                           )
                           .isActive(isActive)
                           .lastActiveAt(otherUser.getLastActiveAt())
                           .build();
               })
               .toList();

       return new PageImpl<>(dtoList, participants.getPageable(), participants.getTotalElements());
   }
   
   @Transactional
   public void softDeleteMessage(Long currentUserId, Long conversationId, Long messageId) {
       Message message = messageRepository.findByIdAndConversationId(messageId, conversationId)
               .orElseThrow(() -> new RuntimeException("Message not found"));

       if (!message.getSender().getId().equals(currentUserId)) {
           throw new RuntimeException("You can delete only your own messages");
       }

       if (message.isSoftDeleted()) {
           throw new RuntimeException("Message already deleted");
       }

       message.setSoftDeleted(true);
       message.setDeletedAt(LocalDateTime.now());
       message.setDeletedBy(currentUserId);

       messageRepository.save(message);
   }
   
   @Transactional
   public int softDeleteSelectedMessages(Long currentUserId, DeleteMessagesRequest request) {
       return messageRepository.softDeleteMessages(
               request.getMessageIds(),
               request.getConversationId(),
               currentUserId,
               LocalDateTime.now(),
               currentUserId
       );
   }
}
