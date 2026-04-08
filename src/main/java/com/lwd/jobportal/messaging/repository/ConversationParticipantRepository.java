package com.lwd.jobportal.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lwd.jobportal.messaging.entity.ConversationParticipant;

import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    // Retrieve all active conversations for a specific user to display in their inbox
    Page<ConversationParticipant> findByUserIdOrderByConversationLastMessageAtDesc(Long userId, Pageable pageable);

    // Get the participant record for a specific user in a specific conversation (for reading/updating statuses)
    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);
}
