//package com.lwd.jobportal.authservice;
//
//import com.lwd.jobportal.repository.UserRepository;
//
//import jakarta.transaction.Transactional;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//public class EmailVerificationCleanupService {
//
//    private final UserRepository userRepository;
//
//    public EmailVerificationCleanupService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    // Runs every 1 hour
//    @Scheduled(fixedRate = 3600000)
//    @Transactional
//    public void cleanupExpiredTokens() {
//        userRepository.clearExpiredTokens(LocalDateTime.now());
//    }
//
//}
