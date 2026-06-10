package com.foodredistribution.foodredistribution.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.foodredistribution.foodredistribution.dto.ClaimDetailResponseDTO;
import com.foodredistribution.foodredistribution.dto.DisputeClaimRequestDTO;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;
import com.foodredistribution.foodredistribution.service.ClaimLifecycleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimLifecycleController {

    private final ClaimLifecycleService claimService;

    public ClaimLifecycleController(ClaimLifecycleService claimService) {
        this.claimService = claimService;
    }

    @PutMapping("/{id}/donor-confirm")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<ClaimDetailResponseDTO> donorConfirm(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(claimService.donorConfirm(id, authentication.getName()));
    }

    @PutMapping("/{id}/receiver-confirm")
    @PreAuthorize("hasRole('RECEIVER') or hasRole('ADMIN')")
    public ResponseEntity<ClaimDetailResponseDTO> receiverConfirm(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(claimService.receiverConfirm(id, authentication.getName()));
    }

    @PutMapping("/{id}/cancel/receiver")
    @PreAuthorize("hasRole('RECEIVER') or hasRole('ADMIN')")
    public ResponseEntity<ClaimDetailResponseDTO> cancelByReceiver(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            Authentication authentication
    ) {
        return ResponseEntity.ok(claimService.cancelByReceiver(id, authentication.getName(), note));
    }


    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<ClaimDetailResponseDTO> markReceiverNoShow(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(claimService.markReceiverNoShow(id, authentication.getName()));
    }

    @PutMapping("/{id}/dispute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClaimDetailResponseDTO> disputeClaim(
            @PathVariable Long id,
            @Valid @RequestBody DisputeClaimRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                claimService.raiseDispute(id, request.getReason(), authentication.getName())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClaimDetailResponseDTO> getClaimDetail(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(claimService.getClaimDetail(id, authentication.getName()));
    }

    @GetMapping("/my-claims")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                claimService.getMyClaims(authentication.getName(), status, page, size)
        );
    }

    @GetMapping("/admin/disputed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClaimDetailResponseDTO>> getAllDisputedClaims(
            Authentication authentication
    ) {
        return ResponseEntity.ok(claimService.getAllDisputedClaims(authentication.getName()));
    }
}
