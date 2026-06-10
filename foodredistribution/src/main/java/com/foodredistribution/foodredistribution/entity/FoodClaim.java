package com.foodredistribution.foodredistribution.entity;

import java.time.LocalDateTime;

import com.foodredistribution.foodredistribution.enums.CancellationReason;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;
import com.foodredistribution.foodredistribution.enums.DisputeReason;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_claims")
public class FoodClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantityClaimed;

    private LocalDateTime claimedAt;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status = ClaimStatus.ACTIVE;

    private boolean donorConfirmed = false;
    private boolean receiverConfirmed = false;

    @Enumerated(EnumType.STRING)
    private CancellationReason cancellationReason;

    @Enumerated(EnumType.STRING)
    private DisputeReason disputeReason;

    private LocalDateTime donorConfirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime disputedAt;
    
    private String cancellationNote;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "food_post_id")
    private FoodPost foodPost;

    public FoodClaim() {
    }

    public FoodClaim(
            Integer quantityClaimed,
            User receiver,
            FoodPost foodPost
    ) {
        this.quantityClaimed = quantityClaimed;
        this.receiver = receiver;
        this.foodPost = foodPost;
        this.claimedAt = LocalDateTime.now();
        this.status = ClaimStatus.ACTIVE;
        this.donorConfirmed = false;
        this.receiverConfirmed = false;
    }

    public Long getId() { return id; }
    public Integer getQuantityClaimed() { return quantityClaimed; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public User getReceiver() { return receiver; }
    public FoodPost getFoodPost() { return foodPost; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public boolean isDonorConfirmed() { return donorConfirmed; }
    public void setDonorConfirmed(boolean donorConfirmed) { this.donorConfirmed = donorConfirmed; }

    public boolean isReceiverConfirmed() { return receiverConfirmed; }
    public void setReceiverConfirmed(boolean receiverConfirmed) { this.receiverConfirmed = receiverConfirmed; }

    public CancellationReason getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(CancellationReason cancellationReason) { this.cancellationReason = cancellationReason; }

    public DisputeReason getDisputeReason() { return disputeReason; }
    public void setDisputeReason(DisputeReason disputeReason) { this.disputeReason = disputeReason; }

    public LocalDateTime getDonorConfirmedAt() { return donorConfirmedAt; }
    public void setDonorConfirmedAt(LocalDateTime donorConfirmedAt) { this.donorConfirmedAt = donorConfirmedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationNote() { return cancellationNote; }
    public void setCancellationNote(String cancellationNote) { this.cancellationNote = cancellationNote; }

    public LocalDateTime getDisputedAt() { return disputedAt; }
    public void setDisputedAt(LocalDateTime disputedAt) { this.disputedAt = disputedAt; }
}