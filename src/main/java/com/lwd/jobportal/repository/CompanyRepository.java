package com.lwd.jobportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lwd.jobportal.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByCompanyName(String companyName);

    Optional<Company> findByCompanyName(String companyName);

    List<Company> findByIsActiveTrue();

    Optional<Company> findByCreatedById(Long userId);
    
    Page<Company> findByIndustry(String industry, Pageable pageable);

}
