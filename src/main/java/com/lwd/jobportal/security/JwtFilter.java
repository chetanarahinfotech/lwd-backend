package com.lwd.jobportal.security;

import com.lwd.jobportal.auth.dto.UserPrincipal;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;

    public JwtFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService customUserDetailsService,
            UserRepository userRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                String tokenType = jwtUtil.extractTokenType(token);
                if (!"access".equals(tokenType)) {
                    writeAuthError(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid token type. Access token required.");
                    return;
                }

                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // ✅ force logout checks on every request
                if (user.isLocked()) {
                    SecurityContextHolder.clearContext();
                    writeAuthError(response, HttpStatus.LOCKED.value(),
                            "Your account is locked. Contact administrator.");
                    return;
                }

                if (Boolean.FALSE.equals(user.getIsActive()) || user.getStatus() == UserStatus.SUSPENDED) {
                    SecurityContextHolder.clearContext();
                    writeAuthError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Your account is suspended. Contact administrator.");
                    return;
                }

                if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
                    SecurityContextHolder.clearContext();
                    writeAuthError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Your account is pending approval.");
                    return;
                }

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                if (!jwtUtil.validateAccessToken(token, userDetails)) {
                    SecurityContextHolder.clearContext();
                    writeAuthError(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Authentication required or token expired.");
                    return;
                }

                Long userId = jwtUtil.extractUserId(token);

                UserPrincipal principal = UserPrincipal.builder()
                        .userId(userId)
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .emailVerified(user.isEmailVerified())
                        .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            writeAuthError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required or token expired.");
            return;

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            writeAuthError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required or token expired.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeAuthError(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"success\":false,\"message\":\"" + message + "\"}"
        );
    }
}