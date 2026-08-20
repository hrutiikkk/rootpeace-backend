package com.checkspace.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String OTP_PREFIX     = "OTP:";
    private static final String ATTEMPT_PREFIX = "OTP_ATTEMPTS:";
    private static final String BLOCK_PREFIX   = "OTP_BLOCKED:";

    private static final int OTP_EXPIRY_MINUTES   = 5;
    private static final int MAX_ATTEMPTS         = 3;
    private static final int BLOCK_HOURS          = 24;

    private static final String SEND_COUNT_PREFIX = "OTP_SEND_COUNT:";
    private static final int MAX_SENDS_PER_DAY = 5;

    public String generateAndSendOtp(String phone) {

        // Check if blocked
        if (isBlocked(phone)) {
            throw new RuntimeException("Too many attempts. Try again after 24 hours.");
        }

        // Check daily send limit — prevents bill bombing
        String sendKey = SEND_COUNT_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(sendKey);
        int sendCount = countStr == null ? 0 : Integer.parseInt(countStr);

        if (sendCount >= MAX_SENDS_PER_DAY) {
            throw new RuntimeException(
                    "Maximum OTP requests reached for today. Try again tomorrow.");
        }

        // Increment send counter with 24hr expiry
        redisTemplate.opsForValue().set(
                sendKey,
                String.valueOf(sendCount + 1),
                24, TimeUnit.HOURS
        );

        String otp = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set(
                OTP_PREFIX + phone, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        redisTemplate.delete(ATTEMPT_PREFIX + phone);

        log.info("OTP for {}: {}", phone, otp);
        return otp;
    }

    public boolean verifyOtp(String phone, String otp) {

        if (isBlocked(phone)) {
            throw new RuntimeException(
                    "Too many wrong attempts. Try again after 24 hours.");
        }

        String storedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + phone);

        if (storedOtp != null && storedOtp.equals(otp)) {
            // Success — clean up
            redisTemplate.delete(OTP_PREFIX + phone);
            redisTemplate.delete(ATTEMPT_PREFIX + phone);
            return true;
        }

        // Wrong OTP — increment attempt counter
        incrementAttempts(phone);
        return false;
    }

    private void incrementAttempts(String phone) {
        String key = ATTEMPT_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(key);
        int count = (countStr == null) ? 0 : Integer.parseInt(countStr);
        count++;

        if (count >= MAX_ATTEMPTS) {
            // Block for 24 hours
            redisTemplate.opsForValue().set(
                    BLOCK_PREFIX + phone, "true", BLOCK_HOURS, TimeUnit.HOURS);
            redisTemplate.delete(key);
            redisTemplate.delete(OTP_PREFIX + phone);
            log.warn("Phone {} BLOCKED for 24 hours after {} wrong attempts", phone, count);
        } else {
            redisTemplate.opsForValue().set(
                    key, String.valueOf(count), OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        }
    }

    public boolean isBlocked(String phone) {
        return redisTemplate.hasKey(BLOCK_PREFIX + phone);
    }

    public int getRemainingAttempts(String phone) {
        String countStr = redisTemplate.opsForValue().get(ATTEMPT_PREFIX + phone);
        int used = (countStr == null) ? 0 : Integer.parseInt(countStr);
        return MAX_ATTEMPTS - used;
    }
}