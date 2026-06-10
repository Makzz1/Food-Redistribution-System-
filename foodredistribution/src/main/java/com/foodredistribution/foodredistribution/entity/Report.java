package com.foodredistribution.foodredistribution.entity;

import java.time.LocalDateTime;

import com.foodredistribution.foodredistribution.enums.ReportReason;

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
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private String description;

    private LocalDateTime reportedAt;

    private boolean reviewed = false;

    private String adminNote;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private FoodClaim claim;

    public Report() {
    }

    public Report(ReportReason reason, String description, User reporter, User reportedUser, FoodClaim claim) {
        this.reason = reason;
        this.description = description;
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.claim = claim;
        this.reportedAt = LocalDateTime.now();
        this.reviewed = false;
    }

    public Long getId() { return id; }
    public ReportReason getReason() { return reason; }
    public String getDescription() { return description; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public User getReporter() { return reporter; }
    public User getReportedUser() { return reportedUser; }
    public FoodClaim getClaim() { return claim; }
}
