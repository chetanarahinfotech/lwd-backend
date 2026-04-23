package com.lwd.jobportal.security;

import com.lwd.jobportal.service.UserActivityService;
import com.lwd.jobportal.util.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ActivityInterceptor implements HandlerInterceptor {

    private final UserActivityService userActivityService;

    public ActivityInterceptor(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        try {
            Long userId = SecurityUtils.getUserId();

            if (userId != null) {
                userActivityService.updateActivity(userId);
            }

        } catch (Exception ignored) {}

        return true;
    }
}
