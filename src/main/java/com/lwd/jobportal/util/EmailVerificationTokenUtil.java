package com.lwd.jobportal.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class EmailVerificationTokenUtil {

    private static final String SECRET_KEY = "lwd-email-verification-secret";

    // 24 hours expiry
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * Generate verification token
     */
    public static String generateToken(String email) {

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    /**
     * Extract email from token
     */
    public static String extractEmail(String token) {

        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token);

        return jwt.getSubject();
    }
}
