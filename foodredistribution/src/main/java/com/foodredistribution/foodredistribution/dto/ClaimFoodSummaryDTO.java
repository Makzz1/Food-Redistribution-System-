package com.foodredistribution.foodredistribution.dto;

public class ClaimFoodSummaryDTO {

    private String receiverName;

    private Integer quantityClaimed;

    public ClaimFoodSummaryDTO() {
    }

    public ClaimFoodSummaryDTO(
            String receiverName,
            Integer quantityClaimed
    ) {
        this.receiverName = receiverName;
        this.quantityClaimed = quantityClaimed;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public Integer getQuantityClaimed() {
        return quantityClaimed;
    }
}