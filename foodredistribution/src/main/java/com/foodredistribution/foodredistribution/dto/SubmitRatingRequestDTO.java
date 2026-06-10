package com.foodredistribution.foodredistribution.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubmitRatingRequestDTO {

    @NotNull(message = "Stars rating is required")
    @Min(value = 1, message = "Minimum rating is 1 star")
    @Max(value = 5, message = "Maximum rating is 5 stars")
    private Integer stars;

    private String review;

    public SubmitRatingRequestDTO() {}

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}
