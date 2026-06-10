package com.foodredistribution.foodredistribution.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodredistribution.foodredistribution.entity.RefreshToken;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Creates a new refresh token for the user.
     * Replaces any existing refresh token (one-per-user policy).
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        // Find existing token or create a new one
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L));
        refreshToken.setUser(user);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifies the refresh token has not expired.
     * Deletes and throws if expired.
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(
                    "Refresh token has expired. Please log in again."
            );
        }

        return token;
    }

    /**
     * Rotates the refresh token — invalidates the old one and creates a new one.
     */
    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        User user = oldToken.getUser();
        return createRefreshToken(user);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
