package com.checkspace.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "builder_spotlights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuilderSpotlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String builderName;

    private String location;
    private String priceFrom;
    private String unitTypes;
    private String websiteUrl;    // null = no website → routes to /contact
    private String bgColor;
    private String tag;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}