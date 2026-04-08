package com.lwd.jobportal.messaging.config;

import java.security.Principal;

public class StompPrincipal implements Principal {
    private final String name;
    private final String email;

    public StompPrincipal(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public String getName() {
        return name; // name will be the userId for easy queue routing /user/{userId}/queue/messages
    }

    public String getEmail() {
        return email;
    }
}
