package com.checkspace.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_remarks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private RemarkType type;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "added_by")
    private Long addedByAdminId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum RemarkType {
        GOOD_SELLER,
        GOOD_BUYER,
        LATE_PAYMENT,
        TRIED_TO_BYPASS,
        FRAUD_SUSPECTED,
        BANNED
    }
}