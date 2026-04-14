package com.lwd.jobportal.repository;

import com.lwd.jobportal.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserIdAndDeletedFalseOrderByUploadedAtDesc(Long userId);

    Optional<Resume> findByIdAndDeletedFalse(Long id);

    Optional<Resume> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    Optional<Resume> findByUserIdAndIsDefaultTrueAndDeletedFalse(Long userId);

    boolean existsByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    long countByUserIdAndDeletedFalse(Long userId);
    
    List<Resume> findByUserIdInAndIsDefaultTrueAndDeletedFalse(List<Long> userIds);
}