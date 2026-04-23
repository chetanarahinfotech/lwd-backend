package com.lwd.jobportal.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByExpiryDateBefore(LocalDateTime now);

    void deleteByUserId(Long userId);
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

}