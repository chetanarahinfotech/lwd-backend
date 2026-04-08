package com.lwd.jobportal.messaging.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.lwd.jobportal.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "conversation_participants",
    uniqueConstraints = {
        // A user can only be in a specific conversation once
        @UniqueConstraint(columnNames = {"user_id", "conversation_id"})
    },
    indexes = {
        @Index(name = "idx_participant_user_convo", columnList = "user_id, conversation_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "lastReadMessageId")
    private Long lastReadMessageId;

    @Column(name = "joinedAt", updatable = false)
    @CreationTimestamp
    private LocalDateTime joinedAt;

    @Column(name = "lastReadAt")
    private LocalDateTime lastReadAt;
}
