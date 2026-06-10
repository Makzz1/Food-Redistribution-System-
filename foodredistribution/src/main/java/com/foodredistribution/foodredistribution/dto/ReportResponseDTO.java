package com.foodredistribution.foodredistribution.dto;

import java.time.LocalDateTime;

import com.foodredistribution.foodredistribution.enums.ReportReason;

public class ReportResponseDTO {

    private Long id;
    private ReportReason reason;
    private String description;
    private String reporterName;
    private String reportedUserName;
    private Long claimId;
    private LocalDateTime reportedAt;
    private boolean reviewed;
    private String adminNote;

    public ReportResponseDTO() {}

    public ReportResponseDTO(Long id, ReportReason reason, String description,
                              String reporterName, String reportedUserName,
                              Long claimId, LocalDateTime reportedAt,
                              boolean reviewed, String adminNote) {
        this.id = id;
        this.reason = reason;
        this.description = description;
        this.reporterName = reporterName;
        this.reportedUserName = reportedUserName;
        this.claimId = claimId;
        this.reportedAt = reportedAt;
        this.reviewed = reviewed;
        this.adminNote = adminNote;
    }

    public Long getId() { return id; }
    public ReportReason getReason() { return reason; }
    public String getDescription() { return description; }
    public String getReporterName() { return reporterName; }
    public String getReportedUserName() { return reportedUserName; }
    public Long getClaimId() { return claimId; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public boolean isReviewed() { return reviewed; }
    public String getAdminNote() { return adminNote; }
}
