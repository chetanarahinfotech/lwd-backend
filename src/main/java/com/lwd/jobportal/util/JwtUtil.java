package com.lwd.jobportal.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("status", user.getStatus() != null ? user.getStatus().name() : null);
        claims.put("emailVerified", user.isEmailVerified());
        claims.put("companyId", user.getCompany() != null ? user.getCompany().getId() : null);
        claims.put("type", "access");

        return buildToken(claims, user.getEmail(), accessExpirationMs);
    }

    public String generateRefreshToken(Long userId, String email) {
        return buildToken(
                Map.of(
                        "userId", userId,
                        "type", "refresh"
                ),
                email,
                refreshExpirationMs
        );
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Role extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        return role != null ? Role.valueOf(role) : null;
    }

    public String extractStatus(String token) {
        return extractClaim(token, claims -> claims.get("status", String.class));
    }

    public Boolean extractEmailVerified(String token) {
        return extractClaim(token, claims -> claims.get("emailVerified", Boolean.class));
    }

    public Long extractCompanyId(String token) {
        Object value = extractClaim(token, claims -> claims.get("companyId"));
        if (value == null) return null;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof Long l) return l;
        return Long.valueOf(value.toString());
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        String type = extractTokenType(token);

        return username.equals(userDetails.getUsername())
                && "access".equals(type)
                && !isTokenExpired(token);
    }

    public boolean validateRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token)) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}