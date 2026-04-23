package com.lwd.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lwd.jobportal.company.CompanyAnalyticsDTO;
import com.lwd.jobportal.entity.Company;
import com.lwd.jobportal.enums.CompanyStatus;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByCompanyName(String companyName);

    Optional<Company> findByCompanyName(String companyName);

    boolean existsByEmail(String email);

    Optional<Company> findByCreatedBy_Id(Long userId);

    Page<Company> findByIndustry(String industry, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(CompanyStatus status);

    @EntityGraph(attributePaths = {"createdBy", "approvedBy"})
    Page<Company> findAll(Pageable pageable);

    @Query("""
            SELECT ja.status, COUNT(ja)
            FROM JobApplication ja
            WHERE ja.job.company.id = :companyId
            GROUP BY ja.status
            """)
    List<Object[]> countByStatusForCompany(@Param("companyId") Long companyId);

    @Query("""
            SELECT new com.lwd.jobportal.company.CompanyAnalyticsDTO(
                COUNT(DISTINCT j.id),
                COUNT(DISTINCT r.id),
                COUNT(DISTINCT a.id),
                COUNT(DISTINCT CASE
                    WHEN j.status = com.lwd.jobportal.enums.JobStatus.OPEN THEN j.id
                END),
                COUNT(DISTINCT CASE
                    WHEN j.status = com.lwd.jobportal.enums.JobStatus.CLOSED THEN j.id
                END)
            )
            FROM Company c
            LEFT JOIN Job j ON j.company.id = c.id
            LEFT JOIN User r ON r.company.id = c.id
                AND r.role = com.lwd.jobportal.enums.Role.RECRUITER
            LEFT JOIN JobApplication a ON a.job.id = j.id
            WHERE c.id = :companyId
            """)
    CompanyAnalyticsDTO getCompanyAnalytics(@Param("companyId") Long companyId);

    @Query("""
            SELECT c
            FROM Company c
            WHERE c.status = com.lwd.jobportal.enums.CompanyStatus.ACTIVE
              AND LOWER(c.companyName) LIKE LOWER(CONCAT(:keyword, '%'))
            ORDER BY c.companyName ASC
            """)
    Page<Company> searchCompanySuggestions(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM Company c
            WHERE (
                :keyword IS NULL OR TRIM(:keyword) = '' OR
                LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(c.industry, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(c.address, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(c.city, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(c.state, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(COALESCE(c.country, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Company> searchCompanies(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM Company c
            WHERE c.status = com.lwd.jobportal.enums.CompanyStatus.ACTIVE
            """)
    List<Company> findAllActiveCompanies();
    
    
    @Query("""
            SELECT c
            FROM Company c
            WHERE c.status = com.lwd.jobportal.enums.CompanyStatus.ACTIVE
              AND (
                  :keyword IS NULL OR TRIM(:keyword) = '' OR
                  LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(COALESCE(c.industry, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(COALESCE(c.address, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(COALESCE(c.city, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(COALESCE(c.state, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(COALESCE(c.country, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Company> searchActiveCompanies(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT c
            FROM Company c
            WHERE c.status = :status
            """)
    Page<Company> findByStatus(@Param("status") CompanyStatus status, Pageable pageable);
}