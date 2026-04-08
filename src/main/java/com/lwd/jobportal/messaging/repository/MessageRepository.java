package com.lwd.jobportal.messaging.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lwd.jobportal.messaging.entity.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Cursor-based pagination (fast offset calculation using 'id < :cursorId')
	 @EntityGraph(attributePaths = {"sender"})
	    @Query("""
	        SELECT m
	        FROM Message m
	        WHERE m.conversation.id = :conversationId
	          AND m.isSoftDeleted = false
	          AND (:cursorId IS NULL OR m.id < :cursorId)
	        ORDER BY m.id DESC
	    """)
	    List<Message> findMessagesByConversationIdBeforeCursor(
	            @Param("conversationId") Long conversationId,
	            @Param("cursorId") Long cursorId,
	            Pageable pageable
	    );

    // Prevent deduplication (frontend sends duplicate events on connection flip/flop)
    boolean existsByClientMessageIdAndSenderId(String clientMessageId, Long senderId);

    // Latest message of a conversation
    Optional<Message> findFirstByConversationIdOrderByIdDesc(Long conversationId);

    // Calculate unread count (messages where id > lastReadMessageId for a specific conversation)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.id > :lastReadId AND m.isSoftDeleted = false")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("lastReadId") Long lastReadId);
    
    @Query("""
            SELECT m.conversation.id, COUNT(m)
            FROM Message m
            JOIN ConversationParticipant cp
                 ON cp.conversation.id = m.conversation.id
            WHERE cp.user.id = :userId
              AND m.conversation.id IN :conversationIds
              AND m.sender.id <> :userId
              AND (
                    cp.lastReadMessageId IS NULL
                    OR m.id > cp.lastReadMessageId
                  )
              AND m.isSoftDeleted = false
            GROUP BY m.conversation.id
        """)
        List<Object[]> countUnreadMessagesForUserConversations(
                @Param("userId") Long userId,
                @Param("conversationIds") List<Long> conversationIds
        );

	@Modifying
    @Query("""
        update Message m
        set m.status = 'DELIVERED'
        where m.conversation.id = :conversationId
          and m.sender.id <> :userId
          and m.status = 'SENT'
    """)
    int markMessagesAsDelivered(Long conversationId, Long userId);

    // When recipient opens the chat and reads them
    @Modifying
    @Query("""
        update Message m
        set m.status = 'READ'
        where m.conversation.id = :conversationId
          and m.sender.id <> :userId
          and m.status in ('SENT', 'DELIVERED')
    """)
    int markMessagesAsRead(Long conversationId, Long userId);
    
    @Query("""
            select m
            from Message m
            where m.id = :messageId
              and m.conversation.id = :conversationId
        """)
        Optional<Message> findByIdAndConversationId(@Param("messageId") Long messageId,
                                                    @Param("conversationId") Long conversationId);

    
    @Modifying
    @Query("""
        update Message m
        set m.isSoftDeleted = true,
            m.deletedAt = :deletedAt,
            m.deletedBy = :deletedBy
        where m.id in :messageIds
          and m.conversation.id = :conversationId
          and m.sender.id = :userId
          and m.isSoftDeleted = false
    """)
    int softDeleteMessages(@Param("messageIds") List<Long> messageIds,
                           @Param("conversationId") Long conversationId,
                           @Param("userId") Long userId,
                           @Param("deletedAt") LocalDateTime deletedAt,
                           @Param("deletedBy") Long deletedBy);

	@Query("""
		    SELECT m
		    FROM Message m
		    WHERE m.id IN (
		        SELECT MAX(m2.id)
		        FROM Message m2
		        WHERE m2.conversation.id IN :conversationIds
		        GROUP BY m2.conversation.id
		    )
		""")
		List<Message> findLastMessagesByConversationIds(
		        @Param("conversationIds") List<Long> conversationIds
		);
}
