package com.lwd.jobportal.repository;

import com.lwd.jobportal.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    // 🔍 Find exact match (case insensitive)
    Optional<Skill> findByNameIgnoreCase(String name);

    // ✅ Check existence
    boolean existsByNameIgnoreCase(String name);

    // 🔎 Autocomplete support
    List<Skill> findByNameContainingIgnoreCase(String keyword);

    // 🔥 Bulk fetch (very useful)
    List<Skill> findByNameIn(List<String> names);
}
