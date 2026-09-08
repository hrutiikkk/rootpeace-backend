package com.checkspace.backend.controller;

import com.checkspace.backend.dto.request.SendOtpRequest;
import com.checkspace.backend.dto.request.VerifyOtpRequest;
import com.checkspace.backend.dto.response.ApiResponse;
import com.checkspace.backend.dto.response.AuthResponse;
import com.checkspace.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

//    @PostMapping("/otp/send")
//    public ResponseEntity<ApiResponse<String>> sendOtp(
//            @Valid @RequestBody SendOtpRequest request) {
//        authService.sendOtp(request);
//        return ResponseEntity.ok(
//                ApiResponse.ok("OTP sent", "OTP sent to " + request.getPhone())
//        );
//    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(
                ApiResponse.ok(response, "Login successful")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me() {
        return ResponseEntity.ok(ApiResponse.ok("authenticated", "Token valid"));
    }

    private final java.util.concurrent.ConcurrentHashMap<String, Integer> ipRequestCount
            = new java.util.concurrent.ConcurrentHashMap<>();

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();
        int count = ipRequestCount.merge(ip, 1, Integer::sum);
        if (count > 10) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.error("Too many requests. Try later."));
        }

        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent", "OTP sent to " + request.getPhone()));
    }
}