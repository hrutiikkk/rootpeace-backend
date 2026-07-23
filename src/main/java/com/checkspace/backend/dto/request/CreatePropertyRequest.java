package com.checkspace.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data
public class CreatePropertyRequest {

    @NotBlank(message = "Title required")
    private String title;

    @NotBlank(message = "City required")
    private String city;

    @NotBlank(message = "Locality required")
    private String locality;

    @NotBlank(message = "Address required")
    private String address;

    private String pincode;

    @NotBlank(message = "Property type required")
    private String propertyType;

    private String bhk;

    @NotNull(message = "Price required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal minPrice;

    private String area;
    private String furnishing;
    private String possessionStatus;
    private String description;

    @NotNull(message = "Seller ID required")
    private Long sellerId;

    private List<MediaItemRequest> photos;
    private MediaItemRequest video;
    private List<MediaItemRequest> documents;
}