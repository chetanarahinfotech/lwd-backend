package com.lwd.jobportal.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.exception.InvalidOperationException;
import com.lwd.jobportal.util.TokenHashUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(User user,
                                           String token,
                                           String deviceInfo,
                                           String ipAddress) {

        if (user == null || user.getId() == null) {
            throw new InvalidOperationException("User is required for refresh token creation");
        }

        if (token == null || token.isBlank()) {
            throw new InvalidOperationException("Refresh token value cannot be null or blank");
        }

        String generatedDeviceId = UUID.randomUUID().toString();
        String tokenHash = tokenHashUtil.sha256(token);

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        refreshTokenRepository.deleteAll(activeTokens);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .tokenHash(tokenHash)
                .user(user)
                .deviceId(generatedDeviceId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
    
    

    public RefreshToken verifyStoredToken(String rawToken) {
        String tokenHash = tokenHashUtil.sha256(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidOperationException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new InvalidOperationException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}