package com.lwd.jobportal.messaging.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lwd.jobportal.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "conversations",
    uniqueConstraints = {
        // Enforce uniqueness for the pair of users
        @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
    },
    indexes = {
        @Index(name = "idx_conversation_last_message", columnList = "lastMessageAt DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // By convention, user1_id should always be less than user2_id to naturally prevent duplicates like (A,B) and (B,A)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "lastMessageAt")
    private LocalDateTime lastMessageAt;

    @Column(name = "lastMessageText", length = 1000)
    private String lastMessageText;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
