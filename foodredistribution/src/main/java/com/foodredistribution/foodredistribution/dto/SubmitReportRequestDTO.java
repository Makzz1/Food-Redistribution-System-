package com.foodredistribution.foodredistribution.dto;

import com.foodredistribution.foodredistribution.enums.ReportReason;

import jakarta.validation.constraints.NotNull;

public class SubmitReportRequestDTO {

    @NotNull(message = "Report reason is required")
    private ReportReason reason;

    private String description;

    public SubmitReportRequestDTO() {}

    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
