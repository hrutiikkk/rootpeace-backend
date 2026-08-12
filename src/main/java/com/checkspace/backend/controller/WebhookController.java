package com.checkspace.backend.controller;

import com.checkspace.backend.model.Payment;
import com.checkspace.backend.model.Property;
import com.checkspace.backend.repository.PaymentRepository;
import com.checkspace.backend.repository.PropertyRepository;
import com.checkspace.backend.repository.UserRepository;
import com.checkspace.backend.service.CashfreeService;
import com.checkspace.backend.service.EmailService;
import com.checkspace.backend.service.WhatsAppService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentRepository  paymentRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository     userRepository;
    private final WhatsAppService    whatsAppService;
    private final EmailService       emailService;
    private final CashfreeService    cashfreeService;

    @Value("${cashfree.secret-key}")
    private String secretKey;

    @PostMapping("/cashfree")
    public ResponseEntity<String> handleCashfree(
            @RequestBody String payload,
            @RequestHeader("x-webhook-signature") String signature,
            @RequestHeader("x-webhook-timestamp") String timestamp) {
        try {
            // 1. Verify signature
            String data = timestamp + payload;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
            String computed = java.util.Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));

            if (!computed.equals(signature)) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            // 2. Parse JSON using Spring Boot's default ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            JsonNode body = mapper.readTree(payload);
            String event = body.get("type").asText();

            log.info("Cashfree webhook received: {}", event);

            // 3. Process payment success
            if ("PAYMENT_SUCCESS_WEBHOOK".equals(event)) {
                String orderId = body.get("data").get("order").get("order_id").asText();
                cashfreeService.verifyPayment(orderId);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Cashfree webhook error: {}", e.getMessage());
            return ResponseEntity.ok("OK"); // Always return 200 to Cashfree so they stop retrying
        }
    }
}