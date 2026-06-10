package com.foodredistribution.foodredistribution.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveDisputeRequestDTO {

    @NotBlank(message = "Resolution is required (COMPLETED or CANCELLED)")
    private String resolution;

    private String adminNote;

    public ResolveDisputeRequestDTO() {}

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}
