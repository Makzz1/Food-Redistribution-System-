package com.foodredistribution.foodredistribution.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodredistribution.foodredistribution.dto.ReportResponseDTO;
import com.foodredistribution.foodredistribution.dto.UserTrustResponseDTO;
import com.foodredistribution.foodredistribution.entity.FoodClaim;
import com.foodredistribution.foodredistribution.entity.Report;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;
import com.foodredistribution.foodredistribution.enums.ReportReason;
import com.foodredistribution.foodredistribution.enums.UserRoleEnum;
import com.foodredistribution.foodredistribution.repository.FoodClaimRepository;
import com.foodredistribution.foodredistribution.repository.ReportRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final FoodClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final TrustService trustService;

    public ReportService(
            ReportRepository reportRepository,
            FoodClaimRepository claimRepository,
            UserRepository userRepository,
            TrustService trustService
    ) {
        this.reportRepository = reportRepository;
        this.claimRepository  = claimRepository;
        this.userRepository   = userRepository;
        this.trustService     = trustService;
    }

    /**
     * File a report. Only allowed when claim is ACTIVE or DONOR_CONFIRMED.
     * Caller reports the OTHER party.
     */
    @Transactional
    public ReportResponseDTO submitReport(
            Long claimId,
            ReportReason reason,
            String description,
            String reporterEmail
    ) {
        User reporter = getUser(reporterEmail);
        FoodClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Guard: reports only allowed when the claim is under dispute
        if (claim.getStatus() != ClaimStatus.DISPUTED) {
            throw new RuntimeException(
                    "Reports can only be filed on DISPUTED claims. "
                    + "Current status: " + claim.getStatus()
                    + ". Please raise a dispute first."
            );
        }

        User donor    = claim.getFoodPost().getDonor();
        User receiver = claim.getReceiver();

        boolean reporterIsDonor    = donor.getId().equals(reporter.getId());
        boolean reporterIsReceiver = receiver.getId().equals(reporter.getId());

        if (!reporterIsDonor && !reporterIsReceiver) {
            throw new RuntimeException("You are not a party to this claim");
        }

        // The reported user is the other party
        User reportedUser = reporterIsDonor ? receiver : donor;

        Report report = new Report(reason, description, reporter, reportedUser, claim);
        reportRepository.save(report);

        // Increment reportCount on the reported user
        reportedUser.setReportCount(
                (reportedUser.getReportCount() != null ? reportedUser.getReportCount() : 0L) + 1
        );
        userRepository.save(reportedUser);

        return toDTO(report);
    }

    /**
     * Get trust profile for any user (public endpoint).
     * Trust score is calculated dynamically.
     */
    public UserTrustResponseDTO getUserTrustProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Double trustScore = trustService.calculateTrustScore(user);
        String trustTier  = trustService.getTrustTier(trustScore);

        return new UserTrustResponseDTO(
                user.getId(),
                user.getName(),
                user.getRating(),
                trustScore,
                trustTier,
                user.getSuccessfulDonations(),
                user.getSuccessfulPickups(),
                user.getNoShowCount(),
                user.getReportCount(),
                user.getDisputeCount()
        );
    }

    // ── Admin methods ───────────────────────────────────────────────────────

    public List<ReportResponseDTO> getUnreviewedReports(String adminEmail) {
        verifyAdmin(adminEmail);
        return reportRepository.findByReviewed(false)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ReportResponseDTO reviewReport(Long reportId, String adminNote, String adminEmail) {
        verifyAdmin(adminEmail);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setReviewed(true);
        report.setAdminNote(adminNote);
        return toDTO(reportRepository.save(report));
    }

    @Transactional
    public void banUser(Long userId, String adminEmail) {
        verifyAdmin(adminEmail);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(true);
        userRepository.save(user);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void verifyAdmin(String email) {
        User user = getUser(email);
        if (user.getRole() != UserRoleEnum.ADMIN) {
            throw new RuntimeException("Access denied — admin only");
        }
    }

    private ReportResponseDTO toDTO(Report r) {
        return new ReportResponseDTO(
                r.getId(),
                r.getReason(),
                r.getDescription(),
                r.getReporter().getName(),
                r.getReportedUser().getName(),
                r.getClaim().getId(),
                r.getReportedAt(),
                r.isReviewed(),
                r.getAdminNote()
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
