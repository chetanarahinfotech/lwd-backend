package com.lwd.jobportal.authservice;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lwd.jobportal.dto.authdto.RegisterRequest;
import com.lwd.jobportal.entity.User;
import com.lwd.jobportal.enums.Role;
import com.lwd.jobportal.enums.UserStatus;
import com.lwd.jobportal.exception.AccountDisabledException;
import com.lwd.jobportal.exception.AccountLockedException;
import com.lwd.jobportal.exception.UserAlreadyExistsException;
import com.lwd.jobportal.repository.UserRepository;
import com.lwd.jobportal.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ================= REGISTER JOB SEEKER =================
    public User registerJobSeeker(RegisterRequest request) {

        validateEmail(request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.JOB_SEEKER)
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .locked(false)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    // ================= REGISTER RECRUITER =================
    public User registerRecruiter(RegisterRequest request) {

        validateEmail(request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.RECRUITER)
                .phone(request.getPhone())
                .status(UserStatus.PENDING_APPROVAL) // Needs approval
                .locked(false)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

 // ================= LOGIN =================
    public String login(String email, String password) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

        } catch (LockedException e) {
            throw new AccountLockedException("Your account is locked. Contact administrator.");
        } catch (DisabledException e) {
            throw new AccountDisabledException("Your account is not active.");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password")
                );

        return jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ================= HELPER METHOD =================
    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered");
        }
    }
}
