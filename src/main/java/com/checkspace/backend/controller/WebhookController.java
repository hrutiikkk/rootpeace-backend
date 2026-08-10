package com.checkspace.backend.controller;

import com.checkspace.backend.model.Payment;
import com.checkspace.backend.model.Property;
import com.checkspace.backend.repository.PaymentRepository;
import com.checkspace.backend.repository.PropertyRepository;
import com.checkspace.backend.repository.UserRepository;
import com.checkspace.backend.service.EmailService;
import com.checkspace.backend.service.WhatsAppService;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpay(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            // Verify signature — proves it's really from Razorpay
            JSONObject options = new JSONObject();
            options.put("json", payload);
            options.put("signature", signature);
            options.put("secret", webhookSecret);

            boolean valid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!valid) {
                log.warn("Invalid Razorpay webhook signature");
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            JSONObject body  = new JSONObject(payload);
            String event     = body.getString("event");
            log.info("Razorpay webhook: {}", event);

            if ("payment.captured".equals(event)) {
                JSONObject paymentEntity = body
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String razorpayOrderId = paymentEntity.getString("order_id");

                paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
                    if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                        payment.setStatus(Payment.PaymentStatus.SUCCESS);
                        payment.setRazorpayPaymentId(paymentEntity.getString("id"));
                        paymentRepository.save(payment);

                        // Update property
                        if (payment.getPropertyId() != null) {
                            propertyRepository.findById(payment.getPropertyId()).ifPresent(property -> {
                                if (payment.getPaymentType() == Payment.PaymentType.VERIFICATION_FEE) {
                                    property.setVerificationFeePaid(true);
                                    property.setStatus(Property.PropertyStatus.UNDER_VERIFICATION);
                                    propertyRepository.save(property);

                                    // Notify seller
                                    userRepository.findById(property.getSellerId()).ifPresent(seller -> {
                                        String invoiceNo = "RP-" + payment.getId();
                                        if (seller.getPhone() != null)
                                            whatsAppService.sendPaymentReceived(
                                                    seller.getPhone(),
                                                    seller.getName() != null ? seller.getName() : "Seller",
                                                    property.getTitle());
                                        if (seller.getEmail() != null)
                                            emailService.sendPaymentInvoice(
                                                    seller.getEmail(),
                                                    seller.getName() != null ? seller.getName() : "Seller",
                                                    seller.getPhone(), property.getTitle(), invoiceNo);
                                    });
                                }
                            });
                        }
                    }
                });
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.ok("OK"); // Always return 200 to Razorpay
        }
    }
}