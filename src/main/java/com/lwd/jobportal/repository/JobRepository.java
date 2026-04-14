package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lwd.jobportal.entity.Job;
import com.lwd.jobportal.enums.JobStatus;
import com.lwd.jobportal.specification.IndustryCount;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
	
	@EntityGraph(attributePaths = {"company"})
	Page<Job> findByCompanyId(Long companyId, Pageable pageable);
	
	@EntityGraph(attributePaths = {"company", "createdBy"})
	List<Job> findByCreatedById(Long userId);
	
    Page<Job> findByCreatedById(Long userId, Pageable pageable);
    
    long countByCompanyId(Long companyId);
    
    @EntityGraph(attributePaths = {
            "company",
            "createdBy",
            "createdBy.company"
    })
    Optional<Job> findByIdAndDeletedFalse(Long id);


    @EntityGraph(attributePaths = {"company"})
    List<Job> findByStatusAndCreatedAtLessThanOrderByCreatedAtDesc(
            JobStatus status,
            LocalDateTime lastSeen,
            Pageable pageable
    );
    
    @Query("""
    		SELECT j FROM Job j
    		JOIN FETCH j.company
    		WHERE j.status = :status
    		ORDER BY j.createdAt DESC
    		""")
    List<Job> findLatestJobsWithCompany(JobStatus status, Pageable pageable);

    Page<Job> findByIndustryIgnoreCaseAndStatus(
            String industry,
            JobStatus status,
            Pageable pageable
    );
    
    @Query("""
    	    SELECT j FROM Job j
    	    JOIN FETCH j.company c
    	    WHERE j.status = 'OPEN'
    	    AND (
    	        LOWER(j.title) LIKE %:keyword%
    	        OR LOWER(j.industry) LIKE %:keyword%
    	        OR LOWER(c.companyName) LIKE %:keyword%
    	    )
    	""")
    	Page<Job> quickSearch(@Param("keyword") String keyword, Pageable pageable);


    
    @Query("""
    	    SELECT j FROM Job j
    	    WHERE j.id NOT IN (
    	        SELECT a.job.id FROM JobApplication a WHERE a.jobSeeker.id = :userId
    	    )
    	      AND (j.industry = :industry OR j.location = :location)
    	    ORDER BY 
    	        CASE 
    	            WHEN j.industry = :industry THEN 0
    	            ELSE 1
    	        END,
    	        j.createdAt DESC
    	""")
    	List<Job> findSuggestedJobs(@Param("userId") Long userId,
    	                            @Param("industry") String industry,
    	                            @Param("location") String location,
    	                            Pageable pageable);


    
    @Query("""
    	    SELECT a.job FROM JobApplication a
    	    WHERE a.jobSeeker.id = :userId
    	    ORDER BY a.appliedAt DESC
    	""")
    	List<Job> findJobsByUserIdOrderByAppliedAtDesc(Long userId);


    
    @Query("""
    	    SELECT j FROM Job j
    	    JOIN FETCH j.createdBy u
    	    JOIN FETCH u.company
    	    WHERE j.status = 'OPEN'
    	    AND j.industry = :industry
    	    AND j.jobType = :jobType
    	    AND j.id <> :jobId
    	""")
    	List<Job> findSimilarJobsWithUserAndCompany(
    	    String industry,
    	    String jobType,
    	    Long jobId
    	);


    @Query("""
    	    SELECT DISTINCT j.title
    	    FROM Job j
    	    WHERE LOWER(j.title) LIKE LOWER(CONCAT(:keyword, '%'))
    	""")
    	List<String> findTitleSuggestions(@Param("keyword") String keyword, Pageable pageable);


    
    @Query("""
    	    SELECT DISTINCT j.location
    	    FROM Job j
    	    WHERE LOWER(j.location) LIKE LOWER(CONCAT(:keyword, '%'))
    	""")
    	List<String> findLocationSuggestions(@Param("keyword") String keyword, Pageable pageable);
    

    @Query("""
    	    SELECT DISTINCT j.company.companyName
    	    FROM Job j
    	    WHERE LOWER(j.company.companyName) LIKE LOWER(CONCAT(:keyword, '%'))
    	""")
    	List<String> findCompanySuggestions(@Param("keyword") String keyword, Pageable pageable);
    

    @Query("""
    	    SELECT DISTINCT j.industry
    	    FROM Job j
    	    WHERE LOWER(j.industry) LIKE LOWER(CONCAT(:keyword, '%'))
    	""")
    	List<String> findIndustrySuggestions(@Param("keyword") String keyword, Pageable pageable);


    
    @Query("""
    	    SELECT j FROM Job j
    	    WHERE j.status = 'OPEN'
    	    ORDER BY j.viewCount DESC
    	""")
    	List<Job> findTrendingJobs(Pageable pageable);

    @Query("""
    	       SELECT j.industry AS industry, COUNT(j) AS count
    	       FROM Job j
    	       WHERE j.status = 'OPEN'
    	       GROUP BY j.industry
    	       ORDER BY COUNT(j) DESC
    	       """)
    	List<IndustryCount> findTopIndustries(Pageable pageable);
    
    
    long countByStatus(JobStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByCompanyIdAndStatus(Long companyId, JobStatus status);
    long countByCreatedByIdAndStatus(Long recruiterId, JobStatus status);
    List<Job> findTop5ByOrderByCreatedAtDesc();
    List<Job> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);
    List<Job> findByCreatedByIdOrderByCreatedAtDesc(Long recruiterId, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.expiresAt BETWEEN :now AND :weekLater")
    List<Job> findJobsExpiringSoon(LocalDateTime now, LocalDateTime weekLater);
    
    @Query("""
    		SELECT j
    		FROM Job j
    		WHERE NOT EXISTS (
    		    SELECT 1
    		    FROM JobApplication ja
    		    WHERE ja.job = j
    		)
    		""")
    		List<Job> findJobsWithoutApplications();

    @Query("SELECT j.industry, COUNT(j) FROM Job j GROUP BY j.industry")
    List<Object[]> countJobsPerIndustry();

    long countByCreatedById(Long recruiterId);
    
    @EntityGraph(attributePaths = {"company"})
    @Query("SELECT j FROM Job j ORDER BY j.createdAt DESC")
    Page<Job> findAllOrderByCreatedAtDesc(Pageable pageable);

    default List<Job> findRecentJobs(int size) {
        return findAllOrderByCreatedAtDesc(PageRequest.of(0, size)).getContent();
    }
    
    
    
    @Query("""
    	    SELECT j FROM Job j
    	    LEFT JOIN j.company c
    	    WHERE j.deleted = false
    	    AND (
    	        LOWER(j.title) LIKE LOWER(CONCAT(:keyword, '%'))
    	        OR LOWER(c.companyName) LIKE LOWER(CONCAT(:keyword, '%'))
    	    )
    	    ORDER BY j.createdAt DESC
    	""")
    	Page<Job> searchJobSuggestions(
    	        @Param("keyword") String keyword,
    	        Pageable pageable
    	);

    
    @EntityGraph(attributePaths = {"company"})
    @Query("""
            SELECT j FROM Job j
            LEFT JOIN j.company c
            WHERE j.deleted = false
            AND (
                :keyword IS NULL
                OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(j.industry) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            ORDER BY j.createdAt DESC
            """)
    Page<Job> searchJobs(
            @Param("keyword") String keyword,
            Pageable pageable
    );
    
    @EntityGraph(attributePaths = {"company"})
    Page<Job> findAll(Pageable pageable);
    
    
    // ================= ADMIN =================
    @Query("""
           SELECT j FROM Job j
           WHERE (:keyword IS NULL 
                  OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.industry) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Job> searchAllJobs(@Param("keyword") String keyword, Pageable pageable);

    // ================= RECRUITER_ADMIN =================
    @Query("""
           SELECT j FROM Job j
           WHERE j.company.id = :companyId
             AND (:keyword IS NULL 
                  OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.industry) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Job> searchJobsByCompany(@Param("companyId") Long companyId,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    // ================= RECRUITER =================
    @Query("""
           SELECT j FROM Job j
           WHERE j.createdBy.id = :userId
             AND (:keyword IS NULL 
                  OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.industry) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Job> searchJobsByCreator(@Param("userId") Long userId,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);



}
