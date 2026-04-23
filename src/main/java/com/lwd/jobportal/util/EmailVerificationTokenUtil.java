package com.lwd.jobportal.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class EmailVerificationTokenUtil {

    private static final String SECRET =
            "4TW+HJFoJ4RMylPoMhVDzxLx8w4ih8nYmAHZ67fNmpA=";
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 hours

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private EmailVerificationTokenUtil() {
    }

    public static String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public static boolean isTokenValid(String token, String email) {
        try {
            String extractedEmail = extractEmail(token);
            return extractedEmail.equalsIgnoreCase(email) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private static Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}