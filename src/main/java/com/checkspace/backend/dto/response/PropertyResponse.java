package com.checkspace.backend.dto.response;
import com.checkspace.backend.model.Property;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertyResponse {

    private Long id;
    private String title;
    private String city;
    private String locality;
    private String propertyType;
    private String bhk;
    private BigDecimal price;
    private String area;
    private String status;
    private boolean visible;
    private LocalDateTime createdAt;
    private List<String> photoUrls;
    private String videoUrl;

    public static PropertyResponse from(Property p) {
        return PropertyResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .city(p.getCity())
                .locality(p.getLocality())
                .propertyType(p.getPropertyType())
                .bhk(p.getBhk())
                .price(p.getPrice())
                .area(p.getArea())
                .status(p.getStatus().name())
                .visible(p.isVisible())
                .createdAt(p.getCreatedAt())
                .build();
    }
}