package com.foodredistribution.foodredistribution.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer stars;

    private String review;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "rated_user_id")
    private User ratedUser;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private FoodClaim claim;

    public Rating() {
    }

    public Rating(Integer stars, String review, User reviewer, User ratedUser, FoodClaim claim) {
        this.stars = stars;
        this.review = review;
        this.reviewer = reviewer;
        this.ratedUser = ratedUser;
        this.claim = claim;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Integer getStars() { return stars; }
    public String getReview() { return review; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getReviewer() { return reviewer; }
    public User getRatedUser() { return ratedUser; }
    public FoodClaim getClaim() { return claim; }
}
