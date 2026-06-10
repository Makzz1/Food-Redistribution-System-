package com.foodredistribution.foodredistribution.dto;

import java.time.LocalDateTime;

import com.foodredistribution.foodredistribution.enums.CancellationReason;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;
import com.foodredistribution.foodredistribution.enums.DisputeReason;

public class ClaimDetailResponseDTO {

    private Long id;
    private Long foodPostId;
    private String foodName;
    private Long donorId;
    private String donorName;
    private Long receiverId;
    private String receiverName;
    private Integer quantityClaimed;
    private ClaimStatus status;
    private boolean donorConfirmed;
    private boolean receiverConfirmed;
    private CancellationReason cancellationReason;
    private String cancellationNote;
    private DisputeReason disputeReason;
    private LocalDateTime claimedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime disputedAt;
    private Double pickupLatitude;
    private Double pickupLongitude;

    public ClaimDetailResponseDTO() {}

    public ClaimDetailResponseDTO(
            Long id, Long foodPostId, String foodName,
            Long donorId, String donorName,
            Long receiverId, String receiverName,
            Integer quantityClaimed, ClaimStatus status,
            boolean donorConfirmed,
            boolean receiverConfirmed,
            CancellationReason cancellationReason,
            String cancellationNote,
            DisputeReason disputeReason,
            LocalDateTime claimedAt, LocalDateTime completedAt,
            LocalDateTime cancelledAt, LocalDateTime disputedAt,
            Double pickupLatitude, Double pickupLongitude
    ) {
        this.id = id;
        this.foodPostId = foodPostId;
        this.foodName = foodName;
        this.donorId = donorId;
        this.donorName = donorName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.quantityClaimed = quantityClaimed;
        this.status = status;
        this.donorConfirmed = donorConfirmed;
        this.receiverConfirmed = receiverConfirmed;
        this.cancellationReason = cancellationReason;
        this.cancellationNote = cancellationNote;
        this.disputeReason = disputeReason;
        this.claimedAt = claimedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.disputedAt = disputedAt;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
    }

    public Long getId() { return id; }
    public Long getFoodPostId() { return foodPostId; }
    public String getFoodName() { return foodName; }
    public Long getDonorId() { return donorId; }
    public String getDonorName() { return donorName; }
    public Long getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }
    public Integer getQuantityClaimed() { return quantityClaimed; }
    public ClaimStatus getStatus() { return status; }
    public boolean isDonorConfirmed() { return donorConfirmed; }
    public boolean isReceiverConfirmed() { return receiverConfirmed; }
    public CancellationReason getCancellationReason() { return cancellationReason; }
    public String getCancellationNote() { return cancellationNote; }
    public DisputeReason getDisputeReason() { return disputeReason; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public LocalDateTime getDisputedAt() { return disputedAt; }
    public Double getPickupLatitude() { return pickupLatitude; }
    public Double getPickupLongitude() { return pickupLongitude; }
}
