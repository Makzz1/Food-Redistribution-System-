package com.foodredistribution.foodredistribution.dto;

import java.time.LocalDateTime;

public class NearbyFoodPostResponseDTO {

    private Long id;
    private String foodName;
    private String description;
    private Integer quantity;
    private String pickupAddress;
    private Double latitude;
    private Double longitude;
    private LocalDateTime expiryTime;
    private String donorName;
    private Double distanceKm;

    public NearbyFoodPostResponseDTO() {
    }

    public NearbyFoodPostResponseDTO(
            Long id,
            String foodName,
            String description,
            Integer quantity,
            String pickupAddress,
            Double latitude,
            Double longitude,
            LocalDateTime expiryTime,
            String donorName,
            Double distanceKm
    ) {
        this.id = id;
        this.foodName = foodName;
        this.description = description;
        this.quantity = quantity;
        this.pickupAddress = pickupAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.expiryTime = expiryTime;
        this.donorName = donorName;
        this.distanceKm = distanceKm;
    }

    public Long getId() {
        return id;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public String getDonorName() {
        return donorName;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }
}
