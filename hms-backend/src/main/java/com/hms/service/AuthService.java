package com.hms.service;

import com.hms.dto.auth.*;
import com.hms.entity.*;
import com.hms.exception.*;
import com.hms.repository.*;
import com.hms.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Tenant tenant = tenantRepository.findBySlugAndDeletedAtIsNull(request.getTenantSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "slug", request.getTenantSlug()));

        if (!tenant.getIsActive()) {
            throw new BadRequestException("Tenant account is inactive");
        }

        User user = userRepository.findByEmailAndTenantIdAndDeletedAtIsNull(request.getEmail(), tenant.getId())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getIsLocked() && user.getLockExpiresAt() != null && user.getLockExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new AccountLockedException("Account is locked. Try again after " + user.getLockExpiresAt());
        }

        if (user.getIsLocked() && user.getLockExpiresAt() != null && user.getLockExpiresAt().isBefore(OffsetDateTime.now())) {
            user.setIsLocked(false);
            user.setFailedLoginAttempts(0);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        user.setFailedLoginAttempts(0);
        user.setIsLocked(false);
        user.setLastLoginAt(OffsetDateTime.now());

        Set<String> roles = user.getRoles().stream().map(Role::getSlug).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getModule() + ":" + p.getAction())
                .collect(Collectors.toSet());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), tenant.getId(), user.getEmail(), roles, permissions);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(OffsetDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000));
        userRepository.save(user);

        log.info("User {} logged in successfully for tenant {}", user.getEmail(), tenant.getSlug());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        User user = userRepository.findByRefreshTokenAndDeletedAtIsNull(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (user.getRefreshTokenExpiresAt() == null || user.getRefreshTokenExpiresAt().isBefore(OffsetDateTime.now())) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiresAt(null);
            userRepository.save(user);
            throw new BadRequestException("Refresh token has expired. Please login again.");
        }

        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(user.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", user.getTenantId()));

        Set<String> roles = user.getRoles().stream().map(Role::getSlug).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getModule() + ":" + p.getAction())
                .collect(Collectors.toSet());

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), tenant.getId(), user.getEmail(), roles, permissions);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiresAt(OffsetDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public void logout(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiresAt(null);
            userRepository.save(user);
        });
    }

    private void handleFailedLogin(User user) {
        int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setIsLocked(true);
            user.setLockExpiresAt(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("Account locked for user {} after {} failed attempts", user.getEmail(), attempts);
        }
        userRepository.save(user);
    }
}
