package com.checkspace.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${msg91.auth-key}")
    private String authKey;

    @Value("${msg91.template-id}")
    private String templateId;

    @Value("${msg91.sender-id}")
    private String senderId;

    private static final String OTP_PREFIX        = "OTP:";
    private static final String ATTEMPT_PREFIX    = "OTP_ATTEMPTS:";
    private static final String BLOCK_PREFIX      = "OTP_BLOCKED:";
    private static final String SEND_COUNT_PREFIX = "OTP_SEND_COUNT:";

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS       = 3;
    private static final int BLOCK_HOURS        = 24;
    private static final int MAX_SENDS_PER_DAY  = 5;

    public String generateAndSendOtp(String phone) {

        if (isBlocked(phone)) {
            throw new RuntimeException("Too many attempts. Try again after 24 hours.");
        }

        // Daily send limit — prevents bill bombing
        String sendKey  = SEND_COUNT_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(sendKey);
        int sendCount   = countStr == null ? 0 : Integer.parseInt(countStr);

        if (sendCount >= MAX_SENDS_PER_DAY) {
            throw new RuntimeException("Maximum OTP requests reached today. Try tomorrow.");
        }

        redisTemplate.opsForValue().set(
                sendKey, String.valueOf(sendCount + 1), 24, TimeUnit.HOURS);

        String otp = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set(
                OTP_PREFIX + phone, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        redisTemplate.delete(ATTEMPT_PREFIX + phone);

        // Send SMS
        sendSmsOtp(phone, otp);

        log.info("OTP sent via SMS to {}", phone);
        return otp;
    }

    @Async
    protected void sendSmsOtp(String phone, String otp) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // The JSON body must map exactly to your template variables (##number##)
            Map<String, String> body = Map.of("number", otp);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            // MSG91 v5 requires authkey, template_id, and mobile as URL query parameters
            String url = String.format("https://control.msg91.com/api/v5/otp?template_id=%s&mobile=91%s&authkey=%s",
                    templateId, phone, authKey);

            restTemplate.postForEntity(url, entity, String.class);

            log.info("SMS OTP dispatched to {}", phone);
        } catch (Exception e) {
            log.error("SMS OTP failed for {}: {}", phone, e.getMessage());
        }
    }

    public boolean verifyOtp(String phone, String otp) {
        if (isBlocked(phone)) {
            throw new RuntimeException("Account blocked for 24 hours.");
        }

        String storedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + phone);

        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(OTP_PREFIX + phone);
            redisTemplate.delete(ATTEMPT_PREFIX + phone);
            return true;
        }

        incrementAttempts(phone);
        return false;
    }

    private void incrementAttempts(String phone) {
        String key      = ATTEMPT_PREFIX + phone;
        String countStr = redisTemplate.opsForValue().get(key);
        int count       = (countStr == null) ? 0 : Integer.parseInt(countStr);
        count++;

        if (count >= MAX_ATTEMPTS) {
            redisTemplate.opsForValue().set(
                    BLOCK_PREFIX + phone, "true", BLOCK_HOURS, TimeUnit.HOURS);
            redisTemplate.delete(key);
            redisTemplate.delete(OTP_PREFIX + phone);
            log.warn("Phone {} blocked after {} wrong OTP attempts", phone, count);
        } else {
            redisTemplate.opsForValue().set(
                    key, String.valueOf(count), OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        }
    }

    public boolean isBlocked(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCK_PREFIX + phone));
    }

    public int getRemainingAttempts(String phone) {
        String countStr = redisTemplate.opsForValue().get(ATTEMPT_PREFIX + phone);
        int used = (countStr == null) ? 0 : Integer.parseInt(countStr);
        return MAX_ATTEMPTS - used;
    }
}