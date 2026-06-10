package com.foodredistribution.foodredistribution.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.foodredistribution.foodredistribution.enums.FoodStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_posts")
public class FoodPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String foodName;
    private String description;
    private Integer quantity;
    private LocalDateTime expiryTime;
    private Double latitude;
    private Double longitude;
    private String pickupAddress;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private FoodStatus status = FoodStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "donor_id")
    private User donor;

    @OneToMany(mappedBy = "foodPost")
    private List<FoodClaim> claims;

    public FoodPost() {
    }

    public FoodPost(String foodName, String description, Integer quantity, LocalDateTime expiryTime,
            Double latitude, Double longitude, String location, User donor) {
        this.foodName = foodName;
        this.description = description;
        this.quantity = quantity;
        this.expiryTime = expiryTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pickupAddress = location;
        this.createdAt = LocalDateTime.now();
        this.status = FoodStatus.AVAILABLE;
        this.donor = donor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
    
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public FoodStatus getStatus() {
        return status;
    }

    public void setStatus(FoodStatus status) {
        this.status = status;
    }

    public User getDonor() {
        return donor;
    }

    public void setDonor(User donor) {
        this.donor = donor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FoodClaim> getClaims() {
        return claims;
    }

    public void setClaims(List<FoodClaim> claims) {
        this.claims = claims;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }
    
}
