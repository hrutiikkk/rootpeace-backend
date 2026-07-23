package com.checkspace.backend.service;

import com.checkspace.backend.dto.request.CreateOrderRequest;
import com.checkspace.backend.dto.request.VerifyPaymentRequest;
import com.checkspace.backend.model.Payment;
import com.checkspace.backend.model.Property;
import com.checkspace.backend.repository.PaymentRepository;
import com.checkspace.backend.repository.PropertyRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // STEP 1 — Create order (IDEMPOTENCY CHECK HAPPENS HERE)
    @Transactional
    public Map<String, Object> createOrder(CreateOrderRequest req) throws Exception {

        // CRITICAL: Check if this idempotency key already exists
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(req.getIdempotencyKey());

        if (existing.isPresent()) {
            Payment payment = existing.get();
            // Already processed — return existing order, DO NOT create new one
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", payment.getRazorpayOrderId());
            response.put("amount", payment.getAmount());
            response.put("status", payment.getStatus().name());
            response.put("duplicate", true);
            return response;
        }

        // RACE CONDITION CHECK — if token amount, ensure property still ACTIVE
        if ("TOKEN_AMOUNT".equals(req.getPaymentType()) && req.getPropertyId() != null) {
            Property property = propertyRepository.findActiveById(req.getPropertyId())
                    .orElseThrow(() -> new RuntimeException(
                            "This property is no longer available — already under negotiation"));
        }

        // Create Razorpay order
        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", req.getAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue()); // paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", req.getIdempotencyKey());

        Order order = client.orders.create(orderRequest);
        String razorpayOrderId = order.get("id");

        // Save payment record BEFORE confirming — status PENDING
        Payment payment = Payment.builder()
                .idempotencyKey(req.getIdempotencyKey())
                .userId(req.getUserId())
                .propertyId(req.getPropertyId())
                .amount(req.getAmount())
                .paymentType(Payment.PaymentType.valueOf(req.getPaymentType()))
                .status(Payment.PaymentStatus.PENDING)
                .razorpayOrderId(razorpayOrderId)
                .build();

        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", razorpayOrderId);
        response.put("amount", req.getAmount());
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);
        response.put("duplicate", false);
        return response;
    }

    // STEP 2 — Verify payment after Razorpay checkout completes
    @Transactional
    public Map<String, Object> verifyPayment(VerifyPaymentRequest req) throws Exception {

        Payment payment = paymentRepository.findByIdempotencyKey(req.getIdempotencyKey())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        // Already verified — don't process twice
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Payment already verified");
            return response;
        }

        // Verify signature — proves payment is genuine, not faked by client
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", req.getRazorpayOrderId());
        options.put("razorpay_payment_id", req.getRazorpayPaymentId());
        options.put("razorpay_signature", req.getRazorpaySignature());

        boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

        if (!isValid) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment signature verification failed");
        }

        // Mark payment success
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        paymentRepository.save(payment);

        // Update property status based on payment type
        if (payment.getPropertyId() != null) {
            Property property = propertyRepository.findById(payment.getPropertyId())
                    .orElseThrow(() -> new RuntimeException("Property not found"));

            if (payment.getPaymentType() == Payment.PaymentType.VERIFICATION_FEE) {
                property.setVerificationFeePaid(true);
                property.setStatus(Property.PropertyStatus.UNDER_VERIFICATION);
            } else if (payment.getPaymentType() == Payment.PaymentType.TOKEN_AMOUNT) {
                // RACE CONDITION PROTECTION — atomic check before locking
                Optional<Property> activeCheck = propertyRepository.findActiveById(property.getId());
                if (activeCheck.isEmpty()) {
                    // Someone else already locked it — refund needed
                    payment.setStatus(Payment.PaymentStatus.REFUNDED);
                    paymentRepository.save(payment);
                    throw new RuntimeException(
                            "Property was just booked by another buyer. Refund will be processed.");
                }
                property.setStatus(Property.PropertyStatus.UNDER_NEGOTIATION);
                property.setTokenPaidBy(payment.getUserId());
                property.setTokenPaidAt(java.time.LocalDateTime.now());
                property.setTokenExpiresAt(java.time.LocalDateTime.now().plusDays(7));
            }
            propertyRepository.save(property);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Payment verified successfully");
        return response;
    }
}