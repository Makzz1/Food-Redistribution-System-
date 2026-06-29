package com.foodredistribution.foodredistribution.controller;

import com.foodredistribution.foodredistribution.annotation.RateLimit;


import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.foodredistribution.foodredistribution.dto.ClaimFoodRequestDTO;
import com.foodredistribution.foodredistribution.dto.ClaimFoodResponseDTO;
import com.foodredistribution.foodredistribution.dto.CreateFoodPostRequestDTO;
import com.foodredistribution.foodredistribution.dto.DonorFoodPostResponseDTO;
import com.foodredistribution.foodredistribution.dto.FoodPostImagesResponseDTO;
import com.foodredistribution.foodredistribution.dto.FoodPostResponseDTO;
import com.foodredistribution.foodredistribution.dto.NearbyFoodPostResponseDTO;
import com.foodredistribution.foodredistribution.dto.UpdateFoodPostRequestDTO;
import com.foodredistribution.foodredistribution.service.FoodPostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/food")
public class FoodPostController {

    private final FoodPostService foodPostService;

    public FoodPostController(FoodPostService foodPostService) {
        this.foodPostService = foodPostService;
    }

    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    @PostMapping
    @RateLimit(requests = 10, window = 60)
    public FoodPostResponseDTO createFoodPost(

            @Valid @RequestBody CreateFoodPostRequestDTO request,
            Authentication authentication
    ) {
        return foodPostService.createFoodPost(request, authentication.getName());
    }

    @GetMapping("/available")
    public Page<FoodPostResponseDTO> getAvailableFoodPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return foodPostService.getAvailableFoodPosts(page, size);
    }

    @PreAuthorize("hasRole('RECEIVER') or hasRole('ADMIN')")
    @PostMapping("/{foodPostId}/claim")
    @RateLimit(requests = 10, window = 60)
    public String claimFood(
            @PathVariable Long foodPostId,
            @Valid @RequestBody ClaimFoodRequestDTO request,
            Authentication authentication
    ) {
        foodPostService.claimFood(foodPostId, request, authentication.getName());
        return "Food claimed successfully";
    }

    @PreAuthorize("hasRole('RECEIVER') or hasRole('ADMIN')")
    @GetMapping("/claims/my")
    public List<ClaimFoodResponseDTO> getMyClaims(Authentication authentication) {
        return foodPostService.getMyClaims(authentication.getName());
    }

    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    @GetMapping("/my-posts")
    public List<DonorFoodPostResponseDTO> getMyFoodPosts(Authentication authentication) {
        return foodPostService.getMyFoodPosts(authentication.getName());
    }

    @PreAuthorize("hasRole('RECEIVER') or hasRole('ADMIN')")
    @GetMapping("/{foodPostId}")
    public FoodPostResponseDTO getFoodPostById(@PathVariable Long foodPostId) {
        return foodPostService.getFoodPostById(foodPostId);
    }

    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    @RateLimit(requests = 10, window = 60)
    public FoodPostResponseDTO updateFoodPost(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodPostRequestDTO request,
            Authentication authentication
    ) {
        return foodPostService.updateFoodPost(id, request, authentication.getName());
    }

    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @RateLimit(requests = 5, window = 60)
    public ResponseEntity<String> deleteFoodPost(
            @PathVariable Long id,
            Authentication authentication
    ) {
        foodPostService.deleteFoodPost(id, authentication.getName());
        return ResponseEntity.ok("Food post deleted successfully");
    }

    // ── Food post image upload ──────────────────────────────────────────────

    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    @PostMapping("/{foodId}/images")
    @RateLimit(requests = 5, window = 60)
    public ResponseEntity<FoodPostImagesResponseDTO> uploadImages(
            @PathVariable Long foodId,
            @RequestParam("images") List<MultipartFile> images,
            Authentication authentication
    ) {
        FoodPostImagesResponseDTO response = foodPostService.uploadImages(foodId, images, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{foodId}/images")
    public ResponseEntity<FoodPostImagesResponseDTO> getImages(@PathVariable Long foodId) {
        return ResponseEntity.ok(foodPostService.getImages(foodId));
    }

    // ── Nearby food search ───────────────────────────────────────────────────

    @PreAuthorize("hasRole('RECEIVER') or hasRole('ADMIN')")
    @GetMapping("/available/nearby")
    public ResponseEntity<Page<NearbyFoodPostResponseDTO>> getNearbyFoodPosts(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(name = "radiusKm", defaultValue = "50") double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NearbyFoodPostResponseDTO> result = foodPostService.getNearbyFoodPosts(
                latitude, longitude, radiusKm, page, size
        );
        return ResponseEntity.ok(result);
    }
}