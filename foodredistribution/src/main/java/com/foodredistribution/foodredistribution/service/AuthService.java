package com.foodredistribution.foodredistribution.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodredistribution.foodredistribution.dto.ForgotPasswordRequestDTO;
import com.foodredistribution.foodredistribution.dto.LoginRequestDTO;
import com.foodredistribution.foodredistribution.dto.LoginResponseDTO;
import com.foodredistribution.foodredistribution.dto.RefreshTokenRequestDTO;
import com.foodredistribution.foodredistribution.dto.RegisterRequestDTO;
import com.foodredistribution.foodredistribution.dto.RegisterResponseDTO;
import com.foodredistribution.foodredistribution.dto.ResetPasswordRequestDTO;
import com.foodredistribution.foodredistribution.entity.RefreshToken;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.jwt.JwtService;
import com.foodredistribution.foodredistribution.repository.RefreshTokenRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;

@Service
public class AuthService {

    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMs;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public RegisterResponseDTO register(RegisterRequestDTO dto) {
        
        String verificationToken =
                UUID.randomUUID().toString();

        if (userRepository.existsByEmail(dto.getEmail())) {
            return new RegisterResponseDTO(null, "Email already exists");
        }

        User user = new User(
            dto.getName(),
            dto.getEmail(),
            passwordEncoder.encode(dto.getPassword()),
            dto.getRole(),
            dto.getPhoneNumber(),
            dto.getLatitude(),
            dto.getLongitude(),
            dto.getLocation()
        );

        user.setVerificationToken(verificationToken);

        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationToken
        );
        return new RegisterResponseDTO(savedUser.getId(), "User registered successfully");
    }

    public LoginResponseDTO Login(LoginRequestDTO dto) {

        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());

        if (userOpt.isEmpty()) {
            return new LoginResponseDTO("Invalid email or password", null, null);
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return new LoginResponseDTO("Invalid email or password", null, null);
        }

        if (user.isBanned()) {
            throw new RuntimeException("Your account has been banned. Please contact support.");
        }

        boolean profileComplete = (user.getRole() != null && user.getLatitude() != null && user.getLongitude() != null);
        String roleStr = user.getRole() != null ? user.getRole().name() : null;

        String accessToken = jwtService.generateToken(dto.getEmail(), roleStr, profileComplete);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponseDTO(
                "Login successful",
                user.getId(),
                accessToken,
                refreshToken.getToken(),
                jwtExpirationMs / 1000
        );
    }

    /**
     * Task 7 — Refresh Token.
     * Validates refresh token, rotates it, and issues a new access token.
     */
    @Transactional
    public LoginResponseDTO refreshAccessToken(RefreshTokenRequestDTO dto) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        // Rotate: invalidate old, create new
        RefreshToken newRefreshToken = refreshTokenService.rotateToken(refreshToken);

        boolean profileComplete = (newRefreshToken.getUser().getRole() != null 
                && newRefreshToken.getUser().getLatitude() != null 
                && newRefreshToken.getUser().getLongitude() != null);
        String roleStr = newRefreshToken.getUser().getRole() != null ? newRefreshToken.getUser().getRole().name() : null;

        String accessToken = jwtService.generateToken(
                newRefreshToken.getUser().getEmail(),
                roleStr,
                profileComplete
        );

        return new LoginResponseDTO(
                "Token refreshed successfully",
                newRefreshToken.getUser().getId(),
                accessToken,
                newRefreshToken.getToken(),
                jwtExpirationMs / 1000
        );
    }

    /**
     * Task 6 — Forgot Password.
     * Generates a reset token, sets a 15-minute expiry, and emails the link.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO dto) {

        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());

        // Don't reveal whether email exists — always return success to caller
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        String resetToken = UUID.randomUUID().toString();

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    /**
     * Task 6 — Reset Password.
     * Validates token, checks expiry, matches passwords, encodes and saves.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository
                .findByPasswordResetToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiry() == null
                || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        userRepository.save(user);
    }

    @Transactional
    public LoginResponseDTO completeProfile(String email, com.foodredistribution.foodredistribution.dto.CompleteProfileRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(dto.getRole());
        user.setLatitude(dto.getLatitude());
        user.setLongitude(dto.getLongitude());
        user.setLocation(dto.getLocation());

        userRepository.save(user);

        // Generate a new token that now has profileComplete = true
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name(), true);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponseDTO(
                "Profile completed successfully",
                user.getId(),
                accessToken,
                refreshToken.getToken(),
                jwtExpirationMs / 1000
        );
    }
}
