package com.checkspace.backend.controller;

import com.checkspace.backend.dto.request.CreateOrderRequest;
import com.checkspace.backend.dto.request.VerifyPaymentRequest;
import com.checkspace.backend.dto.response.ApiResponse;
import com.checkspace.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            Map<String, Object> result = paymentService.createOrder(request);
            return ResponseEntity.ok(ApiResponse.ok(result, "Order created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        try {
            Map<String, Object> result = paymentService.verifyPayment(request);
            return ResponseEntity.ok(ApiResponse.ok(result, "Payment verified"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}