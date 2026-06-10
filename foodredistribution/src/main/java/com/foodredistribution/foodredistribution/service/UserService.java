package com.foodredistribution.foodredistribution.service;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.foodredistribution.foodredistribution.dto.ChangePasswordRequestDTO;
import com.foodredistribution.foodredistribution.dto.ProfileImageResponseDTO;
import com.foodredistribution.foodredistribution.dto.ProfileResponseDTO;
import com.foodredistribution.foodredistribution.dto.UpdateProfileRequestDTO;
import com.foodredistribution.foodredistribution.entity.ProfileImage;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.repository.ProfileImageRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;
import com.foodredistribution.foodredistribution.repository.UserSearchRepository;
import com.foodredistribution.foodredistribution.document.UserDocument;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final UserSearchRepository userSearchRepository;

    public UserService(
            UserRepository userRepository,
            ProfileImageRepository profileImageRepository,
            BCryptPasswordEncoder passwordEncoder,
            StorageService storageService,
            UserSearchRepository userSearchRepository
    ) {
        this.userRepository = userRepository;
        this.profileImageRepository = profileImageRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
        this.userSearchRepository = userSearchRepository;
    }

    // ── Task 1: Rating is part of profile (read-only) ──────────────────────

    public ProfileResponseDTO getUserProfile(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toProfileResponseDTO(user);
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

        // Sync to Elasticsearch if location is present
        if (saved.getLatitude() != null && saved.getLongitude() != null) {
            UserDocument userDoc = userSearchRepository.findById(String.valueOf(saved.getId()))
                    .orElse(new UserDocument());
            
            userDoc.setId(String.valueOf(saved.getId()));
            userDoc.setEmail(saved.getEmail());
            userDoc.setRole(saved.getRole().name());
            userDoc.setLocation(new GeoPoint(saved.getLatitude(), saved.getLongitude()));
            userSearchRepository.save(userDoc);
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

