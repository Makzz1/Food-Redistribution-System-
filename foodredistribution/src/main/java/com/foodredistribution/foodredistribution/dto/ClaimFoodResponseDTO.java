package com.foodredistribution.foodredistribution.dto;

import java.time.LocalDateTime;

public class ClaimFoodResponseDTO {

    private String foodName;

    private Integer quantityClaimed;

    private LocalDateTime claimedAt;

    private String donorName;

    public ClaimFoodResponseDTO() {
    }

    public ClaimFoodResponseDTO(
            String foodName,
            Integer quantityClaimed,
            LocalDateTime claimedAt,
            String donorName
    ) {
        this.foodName = foodName;
        this.quantityClaimed = quantityClaimed;
        this.claimedAt = claimedAt;
        this.donorName = donorName;
    }

    public String getFoodName() {
        return foodName;
    }

    public Integer getQuantityClaimed() {
        return quantityClaimed;
    }

    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }

    public String getDonorName() {
        return donorName;
    }
}