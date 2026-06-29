package com.foodredistribution.foodredistribution.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodredistribution.foodredistribution.entity.FoodClaim;
import com.foodredistribution.foodredistribution.entity.FoodPost;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;

public interface FoodClaimRepository extends JpaRepository<FoodClaim, Long> {

    List<FoodClaim> findByReceiver(User receiver);

    List<FoodClaim> findByFoodPostDonor(User donor);

    Optional<FoodClaim> findByIdAndReceiver(Long id, User receiver);

    Optional<FoodClaim> findByIdAndFoodPostDonor(Long id, User donor);

    List<FoodClaim> findByReceiverAndStatus(User receiver, ClaimStatus status);

    List<FoodClaim> findByFoodPostDonorAndStatus(User donor, ClaimStatus status);

    List<FoodClaim> findByStatus(ClaimStatus status);

    // Check if a post has any blocking claims (prevents deletion)
    boolean existsByFoodPostAndStatusIn(FoodPost foodPost, List<ClaimStatus> statuses);

    // For timeout scheduler: DONOR_CONFIRMED claims where donorConfirmedAt is older than cutoff
    List<FoodClaim> findByStatusAndDonorConfirmedTrueAndDonorConfirmedAtBefore(
            ClaimStatus status, LocalDateTime cutoff);

    // Count queries for admin overview (avoids loading all claims into memory)
    long countByStatus(ClaimStatus status);
}
