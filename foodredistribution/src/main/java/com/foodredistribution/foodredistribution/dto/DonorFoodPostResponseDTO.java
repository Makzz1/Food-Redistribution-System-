package com.foodredistribution.foodredistribution.dto;

import java.util.List;

import com.foodredistribution.foodredistribution.enums.FoodStatus;

public class DonorFoodPostResponseDTO {

    private Long id;
    private String foodName;
    private String description;
    private Integer remainingQuantity;
    private String pickupAddress;
    private java.time.LocalDateTime expiryTime;
    private FoodStatus status;
    private List<ClaimFoodSummaryDTO> claims;

    public DonorFoodPostResponseDTO() {
    }

    public DonorFoodPostResponseDTO(
            Long id,
            String foodName,
            String description,
            Integer remainingQuantity,
            String pickupAddress,
            java.time.LocalDateTime expiryTime,
            FoodStatus status,
            List<ClaimFoodSummaryDTO> claims
    ) {
        this.id = id;
        this.foodName = foodName;
        this.description = description;
        this.remainingQuantity = remainingQuantity;
        this.pickupAddress = pickupAddress;
        this.expiryTime = expiryTime;
        this.status = status;
        this.claims = claims;
    }

    public Long getId() { return id; }
    public String getFoodName() { return foodName; }
    public String getDescription() { return description; }
    public Integer getRemainingQuantity() { return remainingQuantity; }
    public String getPickupAddress() { return pickupAddress; }
    public java.time.LocalDateTime getExpiryTime() { return expiryTime; }
    public FoodStatus getStatus() { return status; }
    public List<ClaimFoodSummaryDTO> getClaims() { return claims; }
}