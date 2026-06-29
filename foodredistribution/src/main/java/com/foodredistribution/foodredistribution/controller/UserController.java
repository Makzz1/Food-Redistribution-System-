package com.foodredistribution.foodredistribution.controller;

import com.foodredistribution.foodredistribution.annotation.RateLimit;


import java.util.Map;

import com.foodredistribution.foodredistribution.dto.ChangePasswordRequestDTO;
import com.foodredistribution.foodredistribution.dto.ProfileImageResponseDTO;
import com.foodredistribution.foodredistribution.dto.ProfileResponseDTO;
import com.foodredistribution.foodredistribution.dto.UpdateProfileRequestDTO;
import com.foodredistribution.foodredistribution.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ── Task 1: Get profile (includes rating) ──────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getUserProfile(
        Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ProfileResponseDTO profileResponseDTO = userService.getUserProfile(userEmail);
        return ResponseEntity.ok(profileResponseDTO);
    }

    // ── Task 4: Update profile ─────────────────────────────────────────────

    @PutMapping("/profile")
    @RateLimit(requests = 10, window = 60)
    public ResponseEntity<ProfileResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ProfileResponseDTO updated = userService.updateProfile(userEmail, request);
        return ResponseEntity.ok(updated);
    }

    // ── Task 5: Change password ────────────────────────────────────────────

    @PutMapping("/change-password")
    @RateLimit(requests = 5, window = 60)
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        Map<String, String> response = userService.changePassword(userEmail, request);
        return ResponseEntity.ok(response);
    }

    // ── Task 2: Profile image upload ───────────────────────────────────────

    @PostMapping("/profile-image")
    @RateLimit(requests = 5, window = 60)
    public ResponseEntity<ProfileImageResponseDTO> uploadProfileImage(
            @RequestParam("image") MultipartFile image,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ProfileImageResponseDTO response = userService.uploadProfileImage(image, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile-image")
    public ResponseEntity<ProfileImageResponseDTO> getProfileImage(
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ProfileImageResponseDTO response = userService.getProfileImage(userEmail);
        return ResponseEntity.ok(response);
    }

}

