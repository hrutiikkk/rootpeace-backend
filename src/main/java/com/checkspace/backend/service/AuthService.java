package com.checkspace.backend.service;

import com.checkspace.backend.dto.request.SendOtpRequest;
import com.checkspace.backend.dto.request.VerifyOtpRequest;
import com.checkspace.backend.dto.response.AuthResponse;
import com.checkspace.backend.model.User;
import com.checkspace.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public void sendOtp(SendOtpRequest request) {
        otpService.generateAndSendOtp(request.getPhone());
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        // Verify OTP from Redis
        boolean valid = otpService.verifyOtp(request.getPhone(), request.getOtp());
        if (!valid) {
            int remaining = otpService.getRemainingAttempts(request.getPhone());
            throw new RuntimeException(
                    "Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        // Find existing user or create new one
        Optional<User> existing = userRepository.findByPhone(request.getPhone());
        boolean isNewUser = existing.isEmpty();

        User user = existing.orElseGet(() -> {
            User.UserRole role = User.UserRole.BUYER;
            try {
                role = User.UserRole.valueOf(
                        request.getRole() != null ? request.getRole().toUpperCase() : "BUYER"
                );
            } catch (Exception ignored) {}

            return userRepository.save(User.builder()
                    .phone(request.getPhone())
                    .role(role)
                    .verified(true)
                    .build());
        });

        // Generate JWT
        String token = jwtService.generateToken(
                user.getId(), user.getPhone(), user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .phone(user.getPhone())
                .newUser(isNewUser)
                .build();
    }
}