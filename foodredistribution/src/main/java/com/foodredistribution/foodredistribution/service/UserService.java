package com.foodredistribution.foodredistribution.service;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.foodredistribution.foodredistribution.dto.ChangePasswordRequestDTO;
import com.foodredistribution.foodredistribution.dto.ProfileImageResponseDTO;
import com.foodredistribution.foodredistribution.dto.ProfileResponseDTO;
import com.foodredistribution.foodredistribution.dto.UpdateProfileRequestDTO;
import com.foodredistribution.foodredistribution.entity.ProfileImage;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.repository.ProfileImageRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StorageService storageService;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    public UserService(
            UserRepository userRepository,
            ProfileImageRepository profileImageRepository,
            BCryptPasswordEncoder passwordEncoder,
            StorageService storageService,
            StringRedisTemplate redisTemplate
    ) {
        this.userRepository = userRepository;
        this.profileImageRepository = profileImageRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
        this.redisTemplate = redisTemplate;
    }

    // ── Task 1: Rating is part of profile (read-only) ──────────────────────

    public ProfileResponseDTO getUserProfile(String userEmail) {
        String cacheKey = "userProfile::" + userEmail;

        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData, ProfileResponseDTO.class);
            }
        } catch (Exception e) {
            // Ignore cache read errors and fallback to DB
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileResponseDTO response = toProfileResponseDTO(user);

        try {
            String json = objectMapper.writeValueAsString(response);
            long baseTtl = 24 * 60 * 60; // 24 hours
            long jitter = (long) (Math.random() * 3600); // 0 to 60 mins
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(baseTtl + jitter));
        } catch (Exception e) {
            // Ignore cache write errors
        }

        return response;
    }

    // ── Task 4: Update profile ──────────────────────────────────────────────

    @Transactional
    public ProfileResponseDTO updateProfile(
            String userEmail,
            UpdateProfileRequestDTO request
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setLocation(request.getLocation());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());

        User saved = userRepository.save(user);

        try {
            redisTemplate.delete("userProfile::" + userEmail);
        } catch (Exception e) {
            // Ignore cache delete error
        }

        return toProfileResponseDTO(saved);
    }

    // ── Task 5: Change password ─────────────────────────────────────────────

    @Transactional
    public Map<String, String> changePassword(
            String userEmail,
            ChangePasswordRequestDTO request
    ) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return Map.of("message", "Password updated successfully");
    }

    // ── Task 2: Profile image upload ────────────────────────────────────────

    @Transactional
    public ProfileImageResponseDTO uploadProfileImage(
            MultipartFile file,
            String userEmail
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = storageService.store(file, "profiles");

        ProfileImage profileImage = profileImageRepository
                .findByUser(user)
                .orElse(new ProfileImage());

        profileImage.setImageUrl(imageUrl);
        profileImage.setUser(user);
        profileImage.setCreatedAt(java.time.LocalDateTime.now());

        profileImageRepository.save(profileImage);

        return new ProfileImageResponseDTO("Profile image uploaded successfully", imageUrl);
    }

    public ProfileImageResponseDTO getProfileImage(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileImage profileImage = profileImageRepository
                .findByUser(user)
                .orElseThrow(() -> new RuntimeException("No profile image found"));

        return new ProfileImageResponseDTO("Profile image retrieved", profileImage.getImageUrl());
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private ProfileResponseDTO toProfileResponseDTO(User user) {
        return new ProfileResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLocation(),
                user.getRole(),
                user.getPhoneVerified(),
                user.getEmailVerified(),
                user.getRating()
        );
    }
}

