package com.lwd.jobportal.searchhistory.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.searchhistory.enums.SearchModule;
import com.lwd.jobportal.searchhistory.enums.SearchType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "search_history",
    indexes = {
        @Index(name = "idx_sh_user_lastseen", columnList = "user_id, last_seen_at"),
        @Index(name = "idx_sh_user_type_lastseen", columnList = "user_id, search_type, last_seen_at"),
        @Index(name = "idx_sh_user_hash", columnList = "user_id, search_hash"),
        @Index(name = "idx_sh_deleted_lastseen", columnList = "deleted, last_seen_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE search_history SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "role",
        nullable = false,
        columnDefinition = "ENUM('ADMIN','JOB_SEEKER','RECRUITER','RECRUITER_ADMIN','SUPER_ADMIN')"
    )
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "module",
        nullable = false,
        length = 50,
        columnDefinition = "ENUM('JOBS','CANDIDATES','APPLICATIONS','COMPANIES','USERS','RECRUITERS','GLOBAL_SEARCH','ADMIN_PANEL','RECRUITER_DASHBOARD','JOB_SEEKER_DASHBOARD')"
    )
    private SearchModule module;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "search_type",
        nullable = false,
        length = 50,
        columnDefinition = "ENUM('JOB','CANDIDATE','APPLICATION','COMPANY','USER','RECRUITER','SKILL','GLOBAL')"
    )
    private SearchType searchType;
    
    @Column(name = "keyword", length = 255)
    private String keyword;

    @Column(name = "normalized_keyword", length = 255)
    private String normalizedKeyword;

    @Lob
    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @Column(name = "search_hash", nullable = false, length = 64)
    private String searchHash;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column(name = "source_page", length = 150)
    private String sourcePage;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "occurrence_count", nullable = false)
    private Integer occurrenceCount;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;
    
    
    
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.deleted == null) {
            this.deleted = false;
        }

        if (this.occurrenceCount == null) {
            this.occurrenceCount = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}