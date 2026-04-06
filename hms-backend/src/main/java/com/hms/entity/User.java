package com.hms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    private String gender;
    private String address;
    private String city;
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    private String country;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "lock_expires_at")
    private OffsetDateTime lockExpiresAt;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private OffsetDateTime refreshTokenExpiresAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
