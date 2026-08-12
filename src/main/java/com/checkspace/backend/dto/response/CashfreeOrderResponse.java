package com.checkspace.backend.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashfreeOrderResponse {
    private String orderId;
    private String paymentSessionId;
    private String orderStatus;
    private String amount;
}