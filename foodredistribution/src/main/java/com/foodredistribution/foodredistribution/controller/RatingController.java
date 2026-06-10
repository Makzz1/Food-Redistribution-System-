package com.foodredistribution.foodredistribution.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodredistribution.foodredistribution.dto.RatingResponseDTO;
import com.foodredistribution.foodredistribution.dto.SubmitRatingRequestDTO;
import com.foodredistribution.foodredistribution.service.RatingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/claim/{claimId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RatingResponseDTO> submitRating(
            @PathVariable Long claimId,
            @Valid @RequestBody SubmitRatingRequestDTO request,
            Authentication authentication
    ) {
        RatingResponseDTO response = ratingService.submitRating(
                claimId,
                request.getStars(),
                request.getReview(),
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingResponseDTO>> getRatingsForUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ratingService.getRatingsForUser(userId));
    }
}
