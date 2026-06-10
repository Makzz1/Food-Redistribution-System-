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
@Table(name = "food_post_images")
public class FoodPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "food_post_id")
    private FoodPost foodPost;

    public FoodPostImage() {
    }

    public FoodPostImage(
            String imageUrl,
            FoodPost foodPost
    ) {
        this.imageUrl = imageUrl;
        this.foodPost = foodPost;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public FoodPost getFoodPost() {
        return foodPost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setFoodPost(FoodPost foodPost) {
        this.foodPost = foodPost;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }



}
