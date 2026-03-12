package com.lwd.jobportal.util;

import java.util.UUID;
import java.time.LocalDateTime;

public class TokenUtil {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static LocalDateTime getExpiry(int hours) {
        return LocalDateTime.now().plusHours(hours);
    }
}
