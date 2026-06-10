package com.foodredistribution.foodredistribution.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodredistribution.foodredistribution.dto.ClaimDetailResponseDTO;
import com.foodredistribution.foodredistribution.dto.ReportResponseDTO;
import com.foodredistribution.foodredistribution.dto.ResolveDisputeRequestDTO;
import com.foodredistribution.foodredistribution.dto.ReviewReportRequestDTO;
import com.foodredistribution.foodredistribution.dto.SubmitReportRequestDTO;
import com.foodredistribution.foodredistribution.dto.UserTrustResponseDTO;
import com.foodredistribution.foodredistribution.service.ClaimLifecycleService;
import com.foodredistribution.foodredistribution.service.ReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final ClaimLifecycleService claimService;

    public ReportController(ReportService reportService, ClaimLifecycleService claimService) {
        this.reportService = reportService;
        this.claimService = claimService;
    }

    @PostMapping("/claim/{claimId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportResponseDTO> submitReport(
            @PathVariable Long claimId,
            @Valid @RequestBody SubmitReportRequestDTO request,
            Authentication authentication
    ) {
        ReportResponseDTO response = reportService.submitReport(
                claimId,
                request.getReason(),
                request.getDescription(),
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/trust")
    public ResponseEntity<UserTrustResponseDTO> getUserTrustProfile(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(reportService.getUserTrustProfile(userId));
    }

    // ── Admin Endpoints ──────────────────────────────────────────────────────

    @GetMapping("/admin/unreviewed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponseDTO>> getUnreviewedReports(
            Authentication authentication
    ) {
        return ResponseEntity.ok(reportService.getUnreviewedReports(authentication.getName()));
    }

    @PutMapping("/admin/{reportId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponseDTO> reviewReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ReviewReportRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                reportService.reviewReport(reportId, request.getAdminNote(), authentication.getName())
        );
    }

    @PutMapping("/admin/claim/{claimId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimDetailResponseDTO> resolveDispute(
            @PathVariable Long claimId,
            @Valid @RequestBody ResolveDisputeRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                claimService.resolveDispute(claimId, request.getResolution(), request.getAdminNote(), authentication.getName())
        );
    }

    @PutMapping("/admin/user/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> banUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        reportService.banUser(userId, authentication.getName());
        return ResponseEntity.ok("User banned successfully");
    }
}
