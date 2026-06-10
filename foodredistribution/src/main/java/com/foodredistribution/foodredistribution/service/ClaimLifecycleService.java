package com.foodredistribution.foodredistribution.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodredistribution.foodredistribution.dto.ClaimDetailResponseDTO;
import com.foodredistribution.foodredistribution.entity.FoodClaim;
import com.foodredistribution.foodredistribution.entity.FoodPost;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.CancellationReason;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;
import com.foodredistribution.foodredistribution.enums.DisputeReason;
import com.foodredistribution.foodredistribution.enums.FoodStatus;
import com.foodredistribution.foodredistribution.repository.FoodClaimRepository;
import com.foodredistribution.foodredistribution.repository.FoodPostRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;

@Service
public class ClaimLifecycleService {

    private final FoodClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final FoodPostRepository foodPostRepository;

    // Configurable via application.properties — change without redeploying
    @Value("${claim.no-show.wait-minutes:30}")
    private int noShowMinWaitMinutes;        // donor must wait this long before marking no-show

    @Value("${claim.dispute.wait-minutes:45}")
    private int disputeMinWaitMinutes;       // both parties must wait before raising a dispute

    public ClaimLifecycleService(
            FoodClaimRepository claimRepository,
            UserRepository userRepository,
            FoodPostRepository foodPostRepository
    ) {
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.foodPostRepository = foodPostRepository;
    }

    // ── Donor confirms food was handed over ────────────────────────────────

    @Transactional
    public ClaimDetailResponseDTO donorConfirm(Long claimId, String donorEmail) {

        User donor = getUser(donorEmail);
        FoodClaim claim = claimRepository.findByIdAndFoodPostDonor(claimId, donor)
                .orElseThrow(() -> new RuntimeException("Claim not found or you are not the donor"));

        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            throw new RuntimeException(
                    "Can only confirm handoff on an ACTIVE claim. Current status: " + claim.getStatus()
            );
        }

        claim.setDonorConfirmed(true);
        claim.setStatus(ClaimStatus.DONOR_CONFIRMED);
        claim.setDonorConfirmedAt(LocalDateTime.now());

        return toDTO(claimRepository.save(claim));
    }

    // ── Receiver confirms food was received → COMPLETED ────────────────────

    @Transactional
    public ClaimDetailResponseDTO receiverConfirm(Long claimId, String receiverEmail) {

        User receiver = getUser(receiverEmail);
        FoodClaim claim = claimRepository.findByIdAndReceiver(claimId, receiver)
                .orElseThrow(() -> new RuntimeException("Claim not found or you are not the receiver"));

        if (claim.getStatus() != ClaimStatus.DONOR_CONFIRMED) {
            throw new RuntimeException(
                    "Can only confirm receipt after donor has confirmed handoff. Current status: " + claim.getStatus()
            );
        }

        claim.setReceiverConfirmed(true);
        claim.setStatus(ClaimStatus.COMPLETED);
        claim.setCompletedAt(LocalDateTime.now());

        // Update trust metrics
        User donor = claim.getFoodPost().getDonor();
        donor.setSuccessfulDonations(
                (donor.getSuccessfulDonations() != null ? donor.getSuccessfulDonations() : 0L) + 1
        );
        userRepository.save(donor);

        receiver.setSuccessfulPickups(
                (receiver.getSuccessfulPickups() != null ? receiver.getSuccessfulPickups() : 0L) + 1
        );
        userRepository.save(receiver);

        return toDTO(claimRepository.save(claim));
    }

    // ── Receiver cancels (only while ACTIVE) → restores food quantity ──────

    @Transactional
    public ClaimDetailResponseDTO cancelByReceiver(Long claimId, String receiverEmail, String note) {
        FoodClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (!claim.getReceiver().getEmail().equals(receiverEmail) &&
            getUser(receiverEmail).getRole() != com.foodredistribution.foodredistribution.enums.UserRoleEnum.ADMIN) {
            throw new RuntimeException("Not authorized to cancel this claim");
        }

        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE claims can be cancelled");
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        claim.setCancellationReason(CancellationReason.RECEIVER_CANCELLED);
        claim.setCancellationNote(note);
        claim.setCancelledAt(LocalDateTime.now());

        restoreFoodQuantity(claim);

        return toDTO(claimRepository.save(claim));
    }

    // ── Donor cancel REMOVED ────────────────────────────────────────────────
    //
    // Once a receiver claims food, the donor cannot cancel the claim.
    // Reason: prevents favouritism and ensures food is not wasted.
    // If the donor has a genuine emergency, they contact admin who can
    // cancel the claim manually via resolveDispute("CANCELLED").
    //
    // Donor CAN still delete their post before any claim is made.

    // ── Donor reports receiver no-show (min 30 min wait) → restores food ───

    @Transactional
    public ClaimDetailResponseDTO markReceiverNoShow(Long claimId, String donorEmail) {

        User donor = getUser(donorEmail);
        FoodClaim claim = claimRepository.findByIdAndFoodPostDonor(claimId, donor)
                .orElseThrow(() -> new RuntimeException("Claim not found or you are not the donor"));

        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            throw new RuntimeException(
                    "Can only mark no-show on ACTIVE claims. Current status: " + claim.getStatus()
            );
        }

        // Enforce minimum wait: donor must give receiver time to arrive
        LocalDateTime earliest = claim.getClaimedAt().plusMinutes(noShowMinWaitMinutes);
        if (LocalDateTime.now().isBefore(earliest)) {
            throw new RuntimeException(
                    "No-show can only be reported " + noShowMinWaitMinutes
                    + " minutes after the claim was made. Please wait a little longer."
            );
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        claim.setCancellationReason(CancellationReason.RECEIVER_NO_SHOW);
        claim.setCancelledAt(LocalDateTime.now());

        // Penalise receiver
        User receiver = claim.getReceiver();
        receiver.setNoShowCount(
                (receiver.getNoShowCount() != null ? receiver.getNoShowCount() : 0L) + 1
        );
        userRepository.save(receiver);

        restoreFoodQuantity(claim);

        return toDTO(claimRepository.save(claim));
    }

    // ── Raise dispute (role-specific guards — dispute is a last resort) ──────
    //
    // DONOR can dispute ACTIVE claims after the wait time — receiver had a fair
    //   chance to arrive. Use no-show for simpler cases.
    //
    // RECEIVER can dispute after the wait time in TWO situations:
    //   1. DONOR_CONFIRMED: receiver went, got the food, but something is wrong
    //      (food spoiled, not as described, quantity mismatch).
    //   2. ACTIVE (after wait): receiver arrived, no food and donor never confirmed.
    //      This is the "phantom donor" scenario — donor is unreachable.
    //
    // Only the ACCUSED party's disputeCount is penalised.

    @Transactional
    public ClaimDetailResponseDTO raiseDispute(
            Long claimId, DisputeReason reason, String userEmail
    ) {
        User caller = getUser(userEmail);
        FoodClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        boolean isDonor    = claim.getFoodPost().getDonor().getId().equals(caller.getId());
        boolean isReceiver = claim.getReceiver().getId().equals(caller.getId());

        if (!isDonor && !isReceiver) {
            throw new RuntimeException("You are not a party to this claim");
        }

        // ── Donor dispute: ACTIVE only, must wait configured time ────────────
        if (isDonor) {
            if (claim.getStatus() != ClaimStatus.ACTIVE) {
                throw new RuntimeException(
                        "Donor can only dispute ACTIVE claims. Current status: " + claim.getStatus()
                );
            }
            LocalDateTime earliest = claim.getClaimedAt().plusMinutes(disputeMinWaitMinutes);
            if (LocalDateTime.now().isBefore(earliest)) {
                throw new RuntimeException(
                        "You can only raise a dispute " + disputeMinWaitMinutes
                        + " minutes after the claim was made. "
                        + "This gives the receiver a fair chance to arrive. "
                        + "If the receiver hasn't shown up, use the no-show endpoint instead."
                );
            }
        }

        // ── Receiver dispute: two valid scenarios ────────────────────────────
        if (isReceiver) {
            boolean isDonorConfirmed = claim.getStatus() == ClaimStatus.DONOR_CONFIRMED;
            boolean isActiveWithWait = claim.getStatus() == ClaimStatus.ACTIVE
                    && LocalDateTime.now().isAfter(
                            claim.getClaimedAt().plusMinutes(disputeMinWaitMinutes));

            if (!isDonorConfirmed && !isActiveWithWait) {
                // Give a specific, helpful error for each case
                if (claim.getStatus() == ClaimStatus.ACTIVE) {
                    long waitLeft = java.time.Duration.between(
                            LocalDateTime.now(),
                            claim.getClaimedAt().plusMinutes(disputeMinWaitMinutes)
                    ).toMinutes();
                    throw new RuntimeException(
                            "You can raise a dispute on an ACTIVE claim only after waiting "
                            + disputeMinWaitMinutes + " minutes (to confirm you arrived and the donor is absent). "
                            + "Please wait approximately " + Math.max(waitLeft, 1) + " more minute(s). "
                            + "If you simply no longer want the food, cancel the claim instead."
                    );
                }
                throw new RuntimeException(
                        "Cannot raise a dispute on a claim with status: " + claim.getStatus()
                );
            }
        }

        claim.setStatus(ClaimStatus.DISPUTED);
        claim.setDisputeReason(reason);
        claim.setDisputedAt(LocalDateTime.now());

        // Only the ACCUSED party is penalised:
        //   Donor disputes → receiver is accused (didn't show / behaviour problem)
        //   Receiver disputes → donor is accused (food not there / not as described)
        User accused = isDonor ? claim.getReceiver() : claim.getFoodPost().getDonor();
        accused.setDisputeCount(
                (accused.getDisputeCount() != null ? accused.getDisputeCount() : 0L) + 1
        );
        userRepository.save(accused);

        return toDTO(claimRepository.save(claim));
    }


    // ── Get claim detail (only for parties involved) ───────────────────────

    public ClaimDetailResponseDTO getClaimDetail(Long claimId, String userEmail) {

        User caller = getUser(userEmail);
        FoodClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        boolean isDonor    = claim.getFoodPost().getDonor().getId().equals(caller.getId());
        boolean isReceiver = claim.getReceiver().getId().equals(caller.getId());

        if (!isDonor && !isReceiver) {
            throw new RuntimeException("Access denied — you are not a party to this claim");
        }

        return toDTO(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimDetailResponseDTO> getAllDisputedClaims(String adminEmail) {
        User admin = getUser(adminEmail);
        if (admin.getRole() != com.foodredistribution.foodredistribution.enums.UserRoleEnum.ADMIN) {
            throw new RuntimeException("Only ADMINs can view all disputes");
        }
        return claimRepository.findByStatus(ClaimStatus.DISPUTED)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Timeout Scheduler ─────────────────────────────────────────────────────

    // ── List my claims ─────────────────────────────────────────────────────

    public java.util.Map<String, Object> getMyClaims(
            String userEmail,
            ClaimStatus statusFilter,
            int page,
            int size
    ) {
        User caller = getUser(userEmail);

        // Collect claims as both receiver and donor roles
        List<FoodClaim> asDonor = statusFilter != null
                ? claimRepository.findByFoodPostDonorAndStatus(caller, statusFilter)
                : claimRepository.findByFoodPostDonor(caller);

        List<FoodClaim> asReceiver = statusFilter != null
                ? claimRepository.findByReceiverAndStatus(caller, statusFilter)
                : claimRepository.findByReceiver(caller);

        // Merge, newest first
        List<ClaimDetailResponseDTO> all = java.util.stream.Stream
                .concat(asDonor.stream(), asReceiver.stream())
                .sorted(java.util.Comparator.comparing(FoodClaim::getClaimedAt).reversed())
                .map(this::toDTO)
                .collect(Collectors.toList());

        // Apply manual pagination
        int total = all.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex   = Math.min(fromIndex + size, total);
        List<ClaimDetailResponseDTO> pageContent = all.subList(fromIndex, toIndex);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content",       pageContent);
        response.put("page",          page);
        response.put("size",          size);
        response.put("totalElements", total);
        response.put("totalPages",    (int) Math.ceil((double) total / size));
        response.put("last",          toIndex >= total);
        return response;
    }

    // ── Admin: resolve a disputed claim ────────────────────────────────────

    @Transactional
    public ClaimDetailResponseDTO resolveDispute(
            Long claimId, String resolution, String adminNote, String adminEmail
    ) {
        FoodClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.DISPUTED) {
            throw new RuntimeException("Only DISPUTED claims can be resolved");
        }

        // resolution should be "COMPLETED" or "CANCELLED"
        if ("COMPLETED".equalsIgnoreCase(resolution)) {
            claim.setStatus(ClaimStatus.COMPLETED);
            claim.setCompletedAt(LocalDateTime.now());
            if (adminNote != null && !adminNote.trim().isEmpty()) {
                claim.setCancellationNote(adminNote);
            }
            // Food stays reduced — receiver confirmed they have it
        } else {
            claim.setStatus(ClaimStatus.CANCELLED);
            claim.setCancellationReason(CancellationReason.ADMIN_RESOLVED);
            if (adminNote != null && !adminNote.trim().isEmpty()) {
                claim.setCancellationNote(adminNote);
            }
            claim.setCancelledAt(LocalDateTime.now());
            // Food was not actually received — restore quantity so others can claim it
            restoreFoodQuantity(claim);
        }

        return toDTO(claimRepository.save(claim));
    }

    // ── Timeout: called by scheduler ───────────────────────────────────────

    @Transactional
    public void timeoutStaleClaims() {

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        List<FoodClaim> staleClaims = claimRepository
                .findByStatusAndDonorConfirmedTrueAndDonorConfirmedAtBefore(
                        ClaimStatus.DONOR_CONFIRMED, cutoff
                );

        for (FoodClaim claim : staleClaims) {
            claim.setStatus(ClaimStatus.DISPUTED);
            claim.setDisputeReason(DisputeReason.RECEIVER_NO_RESPONSE);
            claim.setDisputedAt(LocalDateTime.now());

            // Receiver is the accused — donor gave food, receiver never confirmed
            User receiver = claim.getReceiver();
            receiver.setDisputeCount(
                    (receiver.getDisputeCount() != null ? receiver.getDisputeCount() : 0L) + 1
            );
            userRepository.save(receiver);

            claimRepository.save(claim);
        }
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private ClaimDetailResponseDTO toDTO(FoodClaim c) {
        User donor = c.getFoodPost().getDonor();
        return new ClaimDetailResponseDTO(
                c.getId(),
                c.getFoodPost().getId(),
                c.getFoodPost().getFoodName(),
                donor.getId(),
                donor.getName(),
                c.getReceiver().getId(),
                c.getReceiver().getName(),
                c.getQuantityClaimed(),
                c.getStatus(),
                c.isDonorConfirmed(),
                c.isReceiverConfirmed(),
                c.getCancellationReason(),
                c.getCancellationNote(),
                c.getDisputeReason(),
                c.getClaimedAt(),
                c.getCompletedAt(),
                c.getCancelledAt(),
                c.getDisputedAt(),
                c.getFoodPost().getLatitude(),
                c.getFoodPost().getLongitude()
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    // ── Quantity restoration on cancellation ────────────────────────────────

    /**
     * Adds the claimed quantity back to the food post.
     * If the post was CLAIMED (fully taken), resets it to AVAILABLE so
     * other receivers can claim it again.
     */
    private void restoreFoodQuantity(FoodClaim claim) {
        FoodPost post = claim.getFoodPost();
        int restored = (post.getQuantity() == null ? 0 : post.getQuantity())
                + claim.getQuantityClaimed();
        post.setQuantity(restored);
        if (post.getStatus() == FoodStatus.CLAIMED || post.getStatus() == FoodStatus.AVAILABLE) {
            post.setStatus(FoodStatus.AVAILABLE);
        }
        foodPostRepository.save(post);
    }
}
