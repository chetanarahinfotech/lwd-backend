package com.lwd.jobportal.repository;

import com.lwd.jobportal.entity.Skill;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    // 🔍 Find exact match (case insensitive)
    Optional<Skill> findByNameIgnoreCase(String name);

    // ✅ Check existence
    boolean existsByNameIgnoreCase(String name);

    // 🔎 Autocomplete support
    List<Skill> findByNameContainingIgnoreCase(String keyword);

    // 🔥 Bulk fetch (very useful)
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) IN :names")
    List<Skill> findExistingSkills(@Param("names") Set<String> names);
    
    @Query("""
		       select s.name 
		       from JobSeeker js
		       join js.skills s
		       where js.user.id = :userId
		       """)
		Set<String> findSkillNamesByUserId(@Param("userId") Long userId);
    
    Page<Skill> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    
    @Query("""
    		SELECT s FROM Skill s
    		WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    		ORDER BY s.name ASC
    		""")
    		Page<Skill> searchSkills(String keyword, Pageable pageable);

    @Query("""
    	    SELECT s FROM Skill s
    	    WHERE LOWER(s.name) LIKE LOWER(CONCAT(:keyword, '%'))
    	    ORDER BY s.name ASC
    	""")
    	Page<Skill> searchSkillSuggestions(
    	        @Param("keyword") String keyword,
    	        Pageable pageable
    	);


}
