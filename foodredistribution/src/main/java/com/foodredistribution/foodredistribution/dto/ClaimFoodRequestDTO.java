package com.foodredistribution.foodredistribution.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ClaimFoodRequestDTO {

    @NotNull
    @Min(1)
    private Integer quantityNeeded;

    public ClaimFoodRequestDTO() {
    }

    public Integer getQuantityNeeded() {
        return quantityNeeded;
    }

    public void setQuantityNeeded(Integer quantityNeeded) {
        this.quantityNeeded = quantityNeeded;
    }
}