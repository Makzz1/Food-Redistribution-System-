package com.foodredistribution.foodredistribution.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodredistribution.foodredistribution.dto.RatingResponseDTO;
import com.foodredistribution.foodredistribution.entity.FoodClaim;
import com.foodredistribution.foodredistribution.entity.Rating;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;
import com.foodredistribution.foodredistribution.repository.FoodClaimRepository;
import com.foodredistribution.foodredistribution.repository.RatingRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final FoodClaimRepository claimRepository;
    private final UserRepository userRepository;

    public RatingService(
            RatingRepository ratingRepository,
            FoodClaimRepository claimRepository,
            UserRepository userRepository
    ) {
        this.ratingRepository = ratingRepository;
        this.claimRepository  = claimRepository;
        this.userRepository   = userRepository;
    }

    /**
     * Submit a rating for a COMPLETED claim.
     * Caller (reviewer) rates the other party (ratedUser).
     * Donor rates receiver; receiver rates donor.
     */
    @Transactional
    public RatingResponseDTO submitRating(
            Long claimId,
            Integer stars,
            String review,
            String reviewerEmail
    ) {
        if (stars < 1 || stars > 5) {
            throw new RuntimeException("Stars must be between 1 and 5");
        }

        User reviewer = getUser(reviewerEmail);
        FoodClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.COMPLETED) {
            throw new RuntimeException(
                    "Ratings are only allowed for COMPLETED claims. Current status: " + claim.getStatus()
            );
        }

        // Determine who is being rated (the other party)
        User donor    = claim.getFoodPost().getDonor();
        User receiver = claim.getReceiver();

        boolean reviewerIsDonor    = donor.getId().equals(reviewer.getId());
        boolean reviewerIsReceiver = receiver.getId().equals(reviewer.getId());

        if (!reviewerIsDonor && !reviewerIsReceiver) {
            throw new RuntimeException("You are not a party to this claim");
        }

        if (ratingRepository.existsByReviewerAndClaim(reviewer, claim)) {
            throw new RuntimeException("You have already submitted a rating for this claim");
        }

        User ratedUser = reviewerIsDonor ? receiver : donor;

        // Create rating
        Rating rating = new Rating(stars, review, reviewer, ratedUser, claim);
        ratingRepository.save(rating);

        // Update ratedUser's aggregated rating
        long newSum   = (ratedUser.getRatingSum()    != null ? ratedUser.getRatingSum()    : 0L) + stars;
        long newTotal = (ratedUser.getTotalRatings() != null ? ratedUser.getTotalRatings() : 0L) + 1;

        ratedUser.setRatingSum(newSum);
        ratedUser.setTotalRatings(newTotal);
        ratedUser.setRating((double) newSum / newTotal);
        userRepository.save(ratedUser);

        return toDTO(rating);
    }

    public List<RatingResponseDTO> getRatingsForUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ratingRepository.findByRatedUser(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private RatingResponseDTO toDTO(Rating r) {
        return new RatingResponseDTO(
                r.getId(),
                r.getStars(),
                r.getReview(),
                r.getReviewer().getName(),
                r.getRatedUser().getName(),
                r.getClaim().getId(),
                r.getCreatedAt()
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
