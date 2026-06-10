package com.foodredistribution.foodredistribution.dto;

public class UserTrustResponseDTO {

    private Long userId;
    private String name;
    private Double rating;
    private Double trustScore;
    private String trustTier;          // TRUSTED / GOOD / NEUTRAL / CAUTION / RISKY
    private Long successfulDonations;
    private Long successfulPickups;
    private Long noShowCount;
    private Long reportCount;
    private Long disputeCount;

    public UserTrustResponseDTO() {}

    public UserTrustResponseDTO(Long userId, String name, Double rating, Double trustScore,
                                 String trustTier,
                                 Long successfulDonations, Long successfulPickups,
                                 Long noShowCount, Long reportCount, Long disputeCount) {
        this.userId = userId;
        this.name = name;
        this.rating = rating;
        this.trustScore = trustScore;
        this.trustTier = trustTier;
        this.successfulDonations = successfulDonations;
        this.successfulPickups = successfulPickups;
        this.noShowCount = noShowCount;
        this.reportCount = reportCount;
        this.disputeCount = disputeCount;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public Double getRating() { return rating; }
    public Double getTrustScore() { return trustScore; }
    public String getTrustTier() { return trustTier; }
    public Long getSuccessfulDonations() { return successfulDonations; }
    public Long getSuccessfulPickups() { return successfulPickups; }
    public Long getNoShowCount() { return noShowCount; }
    public Long getReportCount() { return reportCount; }
    public Long getDisputeCount() { return disputeCount; }
}
