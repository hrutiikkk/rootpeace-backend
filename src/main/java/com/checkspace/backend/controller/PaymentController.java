package com.checkspace.backend.controller;

import com.checkspace.backend.dto.request.CashfreeOrderRequest;
import com.checkspace.backend.dto.response.ApiResponse;
import com.checkspace.backend.dto.response.CashfreeOrderResponse;
import com.checkspace.backend.service.CashfreeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CashfreeService cashfreeService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<CashfreeOrderResponse>> createOrder(
            @Valid @RequestBody CashfreeOrderRequest request) {
        try {
            CashfreeOrderResponse result = cashfreeService.createOrder(request);
            return ResponseEntity.ok(ApiResponse.ok(result, "Order created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/verify/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPayment(
            @PathVariable String orderId) {
        try {
            Map<String, Object> result = cashfreeService.verifyPayment(orderId);
            return ResponseEntity.ok(ApiResponse.ok(result, "Verified"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}