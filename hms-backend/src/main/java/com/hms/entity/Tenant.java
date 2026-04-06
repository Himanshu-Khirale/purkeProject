package com.hms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "tenants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String domain;

    @Column(name = "logo_url")
    private String logoUrl;

    private String address;
    private String phone;
    private String email;

    @Column(name = "subscription_plan")
    private String subscriptionPlan;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(length = 4000)
    private String settings;

    @Column(name = "created_at")
    private java.time.OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private java.time.OffsetDateTime deletedAt;
}
