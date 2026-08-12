package com.checkspace.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CashfreeOrderRequest {

    @NotNull
    private Long userId;

    private Long propertyId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String paymentType;

    @NotNull
    private String idempotencyKey;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
}