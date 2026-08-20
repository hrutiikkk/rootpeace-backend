package com.checkspace.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_blacklist", indexes = {
        @Index(name = "idx_address", columnList = "address_hash"),
        @Index(name = "idx_owner_phone", columnList = "owner_phone")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address_hash")
    private String addressHash; // MD5 of full address

    @Column(name = "owner_phone")
    private String ownerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistReason reason;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "added_by")
    private Long addedByAdminId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum BlacklistReason {
        FAKE_DOCUMENTS,
        DISPUTED_OWNERSHIP,
        FRAUD_ATTEMPT,
        COURT_CASE_PENDING,
        DUPLICATE_LISTING,
        SELLER_BANNED
    }
}