package com.lwd.jobportal.auth;

import java.time.LocalDateTime;

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
                                           String rawToken,
                                           String deviceId,
                                           String deviceInfo,
                                           String ipAddress) {

        String tokenHash = tokenHashUtil.sha256(rawToken);

        // remove only same device token
        refreshTokenRepository.deleteByUserIdAndDeviceId(user.getId(), deviceId);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .deviceId(deviceId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .tokenVersion(user.getTokenVersion())
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
        
        if (refreshToken.getTokenVersion() != refreshToken.getUser().getTokenVersion()) {
            throw new InvalidOperationException("Token invalid due to version change (logout all)");
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