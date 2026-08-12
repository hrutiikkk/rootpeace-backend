package com.checkspace.backend.service;

import com.checkspace.backend.dto.request.CashfreeOrderRequest;
import com.checkspace.backend.dto.response.CashfreeOrderResponse;
import com.checkspace.backend.model.Payment;
import com.checkspace.backend.model.Property;
import com.checkspace.backend.repository.PaymentRepository;
import com.checkspace.backend.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashfreeService {

    private final PaymentRepository  paymentRepository;
    private final PropertyRepository propertyRepository;
    private final RestTemplate       restTemplate;

    @Value("${cashfree.app-id}")
    private String appId;

    @Value("${cashfree.secret-key}")
    private String secretKey;

    @Value("${cashfree.base-url}")
    private String baseUrl;

    @Transactional
    public CashfreeOrderResponse createOrder(CashfreeOrderRequest req) {

        // Idempotency check — same key = return existing
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(req.getIdempotencyKey());
        if (existing.isPresent()) {
            Payment p = existing.get();
            return CashfreeOrderResponse.builder()
                    .orderId(p.getRazorpayOrderId())
                    .orderStatus(p.getStatus().name())
                    .amount(p.getAmount().toString())
                    .build();
        }

        // Race condition check for token payment
        if ("TOKEN_AMOUNT".equals(req.getPaymentType()) && req.getPropertyId() != null) {
            propertyRepository.findActiveById(req.getPropertyId())
                    .orElseThrow(() -> new RuntimeException(
                            "Property no longer available — already under negotiation"));
        }

        // Build Cashfree order request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-version", "2023-08-01");
        headers.set("x-client-id", appId);
        headers.set("x-client-secret", secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("order_id",       req.getIdempotencyKey());
        body.put("order_amount",   req.getAmount());
        body.put("order_currency", "INR");
        body.put("customer_details", Map.of(
                "customer_id",    "user_" + req.getUserId(),
                "customer_name",  req.getCustomerName()  != null ? req.getCustomerName()  : "RootPeace User",
                "customer_email", req.getCustomerEmail() != null ? req.getCustomerEmail() : "user@rootpeace.com",
                "customer_phone", req.getCustomerPhone() != null ? req.getCustomerPhone() : "9999999999"
        ));
        body.put("order_meta", Map.of(
                "return_url", "https://rootpeace.com/payment-success?order_id={order_id}"
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/orders", entity, Map.class);

        Map<String, Object> data = response.getBody();
        if (data == null) throw new RuntimeException("Cashfree returned empty response");

        String orderId         = (String) data.get("order_id");
        String paymentSession  = (String) data.get("payment_session_id");

        // Save payment record with PENDING status
        Payment payment = Payment.builder()
                .idempotencyKey(req.getIdempotencyKey())
                .userId(req.getUserId())
                .propertyId(req.getPropertyId())
                .amount(req.getAmount())
                .paymentType(Payment.PaymentType.valueOf(req.getPaymentType()))
                .status(Payment.PaymentStatus.PENDING)
                .razorpayOrderId(orderId) // reusing field for Cashfree order ID
                .build();
        paymentRepository.save(payment);

        return CashfreeOrderResponse.builder()
                .orderId(orderId)
                .paymentSessionId(paymentSession)
                .orderStatus("PENDING")
                .amount(req.getAmount().toString())
                .build();
    }

    @Transactional
    public Map<String, Object> verifyPayment(String orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-version", "2023-08-01");
        headers.set("x-client-id", appId);
        headers.set("x-client-secret", secretKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/orders/" + orderId,
                HttpMethod.GET, entity, Map.class);

        Map<String, Object> data   = response.getBody();
        String orderStatus         = (String) data.get("order_status");

        Map<String, Object> result = new HashMap<>();

        if ("PAID".equals(orderStatus)) {
            paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                    payment.setStatus(Payment.PaymentStatus.SUCCESS);
                    paymentRepository.save(payment);

                    if (payment.getPropertyId() != null) {
                        propertyRepository.findById(payment.getPropertyId()).ifPresent(property -> {
                            if (payment.getPaymentType() == Payment.PaymentType.VERIFICATION_FEE) {
                                property.setVerificationFeePaid(true);
                                property.setStatus(Property.PropertyStatus.UNDER_VERIFICATION);
                                propertyRepository.save(property);
                            }
                        });
                    }
                }
            });
            result.put("status", "SUCCESS");
            result.put("message", "Payment verified");
        } else {
            result.put("status", orderStatus);
            result.put("message", "Payment not completed");
        }

        return result;
    }
}