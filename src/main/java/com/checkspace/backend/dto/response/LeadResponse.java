package com.checkspace.backend.dto.response;

import com.checkspace.backend.model.Property;
import com.checkspace.backend.util.PhoneMaskUtil;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadResponse {
    private Long id;
    private String title;
    private String city;
    private String locality;
    private BigDecimal price;
    private String status;
    private String maskedSellerPhone;
    private LocalDateTime createdAt;

    public static LeadResponse from(Property p, String sellerPhone) {
        return LeadResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .city(p.getCity())
                .locality(p.getLocality())
                .price(p.getPrice())
                .status(p.getStatus().name())
                .maskedSellerPhone(PhoneMaskUtil.mask(sellerPhone))
                .createdAt(p.getCreatedAt())
                .build();
    }
}