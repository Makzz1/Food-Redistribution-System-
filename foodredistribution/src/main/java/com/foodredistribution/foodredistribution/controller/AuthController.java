package com.foodredistribution.foodredistribution.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodredistribution.foodredistribution.dto.ForgotPasswordRequestDTO;
import com.foodredistribution.foodredistribution.dto.LoginRequestDTO;
import com.foodredistribution.foodredistribution.dto.LoginResponseDTO;
import com.foodredistribution.foodredistribution.dto.RefreshTokenRequestDTO;
import com.foodredistribution.foodredistribution.dto.RegisterRequestDTO;
import com.foodredistribution.foodredistribution.dto.RegisterResponseDTO;
import com.foodredistribution.foodredistribution.dto.ResetPasswordRequestDTO;
import com.foodredistribution.foodredistribution.service.AuthService;
import com.foodredistribution.foodredistribution.service.EmailService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public RegisterResponseDTO register(@Valid @RequestBody RegisterRequestDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.Login(dto);
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token) {
        emailService.verifyEmail(token);
        return "Email verified successfully";
    }

    // ── Task 6: Forgot Password ────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO dto
    ) {
        authService.forgotPassword(dto);
        return ResponseEntity.ok(Map.of(
                "message",
                "If an account with that email exists, a password reset link has been sent."
        ));
    }

    // ── Task 6: Reset Password ─────────────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO dto
    ) {
        authService.resetPassword(dto);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // ── Task 7: Refresh Token ──────────────────────────────────────────────

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO dto
    ) {
        LoginResponseDTO response = authService.refreshAccessToken(dto);
        return ResponseEntity.ok(response);
    }

}

