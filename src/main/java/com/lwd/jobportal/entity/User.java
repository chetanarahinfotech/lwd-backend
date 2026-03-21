package com.lwd.jobportal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
    },
    indexes = {

        // Search by name
        @Index(name = "idx_user_name", columnList = "name"),

        // Search by email
        @Index(name = "idx_user_email", columnList = "email"),

        // Search by phone
        @Index(name = "idx_user_phone", columnList = "phone"),

        // Filter by role
        @Index(name = "idx_user_role", columnList = "role"),

        // Filter by status
        @Index(name = "idx_user_status", columnList = "status"),
        
        @Index(name = "idx_user_role_name", columnList = "role,name")


    }
)

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;
    
    private String profileImageUrl;

    @Column(nullable = false)
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status; // PENDING, ACTIVE, BLOCKED

    @Column(length = 15)
    private String phone;

    @Column(nullable = false)
    private Boolean isActive = true;
    
    private LocalDateTime lastActiveAt;
    
    @Column(nullable = false)
    private boolean locked = false;
    
    @Column(nullable = false)
    private boolean emailVerified = false;

    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne(
    	    mappedBy = "user",
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true,
    	    fetch = FetchType.LAZY
    	)
    	@JsonIgnore
    	private JobSeeker jobSeekerProfile;



    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Automatically set timestamps
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
