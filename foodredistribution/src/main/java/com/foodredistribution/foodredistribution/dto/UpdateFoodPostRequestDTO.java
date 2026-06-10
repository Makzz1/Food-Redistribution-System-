package com.foodredistribution.foodredistribution.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateFoodPostRequestDTO {

    @NotBlank
    private String foodName;

    @NotBlank
    private String description;

    @NotNull
    private Integer quantity;

    @Future
    private LocalDateTime expiryTime;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String location;

    public UpdateFoodPostRequestDTO() {
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

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }
}