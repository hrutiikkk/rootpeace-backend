package com.checkspace.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "properties", indexes = {
        @Index(name = "idx_city_status", columnList = "city, status"),
        @Index(name = "idx_visible_status", columnList = "is_visible, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String locality;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    private String pincode;

    @Column(nullable = false)
    private String propertyType;

    private String bhk;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice; // private — only team sees this

    private String area;
    private String furnishing;
    private String possessionStatus;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;

    @Builder.Default
    @Column(name = "is_visible", nullable = false)
    private boolean visible = false;

    @Builder.Default
    @Column(name = "verification_fee_paid")
    private boolean verificationFeePaid = false;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "assigned_to")
    private Long assignedTo; // employee ID

    @Column(name = "token_paid_by")
    private Long tokenPaidBy; // buyer who paid token

    @Column(name = "token_paid_at")
    private LocalDateTime tokenPaidAt;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PropertyStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum PropertyStatus {
        PENDING,           // submitted, not yet verified
        UNDER_VERIFICATION,// team is checking
        VERIFIED,          // approved, visible to buyers
        REJECTED,          // failed verification
        ACTIVE,            // live for buyers
        UNDER_NEGOTIATION, // token paid by a buyer
        SOLD,              // deal closed
        WITHDRAWN          // seller pulled out
    }
}