package com.foodredistribution.foodredistribution.dto;

import java.time.LocalDateTime;

public class RatingResponseDTO {

    private Long id;
    private Integer stars;
    private String review;
    private String reviewerName;
    private String ratedUserName;
    private Long claimId;
    private LocalDateTime createdAt;

    public RatingResponseDTO() {}

    public RatingResponseDTO(Long id, Integer stars, String review,
                              String reviewerName, String ratedUserName,
                              Long claimId, LocalDateTime createdAt) {
        this.id = id;
        this.stars = stars;
        this.review = review;
        this.reviewerName = reviewerName;
        this.ratedUserName = ratedUserName;
        this.claimId = claimId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Integer getStars() { return stars; }
    public String getReview() { return review; }
    public String getReviewerName() { return reviewerName; }
    public String getRatedUserName() { return ratedUserName; }
    public Long getClaimId() { return claimId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
