package com.foodredistribution.foodredistribution.dto;

import com.foodredistribution.foodredistribution.enums.DisputeReason;

import jakarta.validation.constraints.NotNull;

public class DisputeClaimRequestDTO {

    @NotNull(message = "Dispute reason is required")
    private DisputeReason reason;

    private String description;

    public DisputeClaimRequestDTO() {}

    public DisputeReason getReason() { return reason; }
    public void setReason(DisputeReason reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
