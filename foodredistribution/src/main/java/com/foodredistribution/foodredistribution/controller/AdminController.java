package com.foodredistribution.foodredistribution.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodredistribution.foodredistribution.repository.FoodPostRepository;
import com.foodredistribution.foodredistribution.repository.FoodClaimRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;
import com.foodredistribution.foodredistribution.service.ReportService;
import com.foodredistribution.foodredistribution.dto.UserTrustResponseDTO;
import com.foodredistribution.foodredistribution.enums.FoodStatus;
import com.foodredistribution.foodredistribution.entity.FoodPost;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final FoodPostRepository foodPostRepository;
    private final FoodClaimRepository foodClaimRepository;
    private final UserRepository userRepository;
    private final ReportService reportService;

    public AdminController(FoodPostRepository foodPostRepository, FoodClaimRepository foodClaimRepository, UserRepository userRepository, ReportService reportService) {
        this.foodPostRepository = foodPostRepository;
        this.foodClaimRepository = foodClaimRepository;
        this.userRepository = userRepository;
        this.reportService = reportService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Map<String, Object>>> getAllPosts() {
        return ResponseEntity.ok(foodPostRepository.findAll().stream().map(post -> Map.<String, Object>of(
                "id", post.getId(),
                "foodName", post.getFoodName(),
                "description", post.getDescription() != null ? post.getDescription() : "",
                "quantity", post.getQuantity(),
                "status", post.getStatus().name(),
                "donorName", post.getDonor().getName(),
                "expiryTime", String.valueOf(post.getExpiryTime())
        )).collect(Collectors.toList()));
    }

    @GetMapping("/claims")
    public ResponseEntity<List<Map<String, Object>>> getAllClaims() {
        return ResponseEntity.ok(foodClaimRepository.findAll().stream().map(claim -> Map.<String, Object>of(
                "id", claim.getId(),
                "foodName", claim.getFoodPost().getFoodName(),
                "donorName", claim.getFoodPost().getDonor().getName(),
                "receiverName", claim.getReceiver().getName(),
                "quantityClaimed", claim.getQuantityClaimed(),
                "status", claim.getStatus().name(),
                "claimedAt", String.valueOf(claim.getClaimedAt())
        )).collect(Collectors.toList()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserTrustResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(user -> reportService.getUserTrustProfile(user.getId()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStats() {
        long totalUsers = userRepository.count();
        long totalPosts = foodPostRepository.count();
        long totalClaims = foodClaimRepository.count();

        // Use count queries instead of loading all claims into memory
        long activeDisputes = foodClaimRepository.countByStatus(
                com.foodredistribution.foodredistribution.enums.ClaimStatus.DISPUTED);
        long successfulDonations = foodClaimRepository.countByStatus(
                com.foodredistribution.foodredistribution.enums.ClaimStatus.COMPLETED);

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalPosts", totalPosts,
                "totalClaims", totalClaims,
                "activeDisputes", activeDisputes,
                "successfulDonations", successfulDonations
        ));
    }

    @PutMapping("/posts/{id}/delete")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        FoodPost post = foodPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        boolean hasCompletedClaim = post.getClaims().stream()
                .anyMatch(claim -> "COMPLETED".equals(claim.getStatus().name()));
        
        if (hasCompletedClaim) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot delete a post with a completed claim."));
        }
        
        post.setStatus(FoodStatus.DELETED);
        foodPostRepository.save(post);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully."));
    }

    @PutMapping("/posts/{id}/restore")
    public ResponseEntity<?> restorePost(@PathVariable Long id) {
        FoodPost post = foodPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (post.getStatus() != FoodStatus.DELETED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post is not deleted."));
        }
        
        if (post.getExpiryTime().isBefore(java.time.LocalDateTime.now())) {
            post.setStatus(FoodStatus.EXPIRED);
        } else {
            post.setStatus(FoodStatus.AVAILABLE);
        }
        
        foodPostRepository.save(post);
        return ResponseEntity.ok(Map.of("message", "Post restored successfully."));
    }
}
