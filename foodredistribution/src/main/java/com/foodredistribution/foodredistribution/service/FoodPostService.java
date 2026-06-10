package com.foodredistribution.foodredistribution.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.foodredistribution.foodredistribution.dto.ClaimFoodRequestDTO;
import com.foodredistribution.foodredistribution.dto.ClaimFoodResponseDTO;
import com.foodredistribution.foodredistribution.dto.ClaimFoodSummaryDTO;
import com.foodredistribution.foodredistribution.dto.CreateFoodPostRequestDTO;
import com.foodredistribution.foodredistribution.dto.DonorFoodPostResponseDTO;
import com.foodredistribution.foodredistribution.dto.FoodPostImagesResponseDTO;
import com.foodredistribution.foodredistribution.dto.FoodPostResponseDTO;
import com.foodredistribution.foodredistribution.dto.NearbyFoodPostResponseDTO;
import com.foodredistribution.foodredistribution.dto.UpdateFoodPostRequestDTO;
import com.foodredistribution.foodredistribution.entity.FoodClaim;
import com.foodredistribution.foodredistribution.entity.FoodPost;
import com.foodredistribution.foodredistribution.entity.FoodPostImage;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.ClaimStatus;
import com.foodredistribution.foodredistribution.enums.FoodStatus;
import com.foodredistribution.foodredistribution.enums.UserRoleEnum;
import com.foodredistribution.foodredistribution.exception.EmailNotVerifiedException;
import com.foodredistribution.foodredistribution.exception.FoodNotFoundException;
import com.foodredistribution.foodredistribution.exception.InsufficientQuantityException;
import com.foodredistribution.foodredistribution.exception.UnauthorizedActionException;
import com.foodredistribution.foodredistribution.repository.FoodClaimRepository;
import com.foodredistribution.foodredistribution.repository.FoodPostImageRepository;
import com.foodredistribution.foodredistribution.repository.FoodPostRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;
import com.foodredistribution.foodredistribution.repository.FoodPostSearchRepository;
import com.foodredistribution.foodredistribution.repository.UserSearchRepository;
import com.foodredistribution.foodredistribution.document.FoodPostDocument;
import com.foodredistribution.foodredistribution.document.UserDocument;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import com.foodredistribution.foodredistribution.utils.GeoUtils;

@Service
public class FoodPostService {

    private final FoodPostRepository foodPostRepository;
    private final UserRepository userRepository;
    private final FoodClaimRepository foodClaimRepository;
    private final EmailService emailService;
    private final FoodPostImageRepository foodPostImageRepository;
    private final StorageService storageService;
    private final FoodPostSearchRepository foodPostSearchRepository;
    private final UserSearchRepository userSearchRepository;

    public FoodPostService(
            FoodPostRepository foodPostRepository,
            UserRepository userRepository,
            FoodClaimRepository foodClaimRepository,
            EmailService emailService,
            FoodPostImageRepository foodPostImageRepository,
            StorageService storageService,
            FoodPostSearchRepository foodPostSearchRepository,
            UserSearchRepository userSearchRepository
    ) {
        this.foodPostRepository = foodPostRepository;
        this.userRepository = userRepository;
        this.foodClaimRepository = foodClaimRepository;
        this.emailService = emailService;
        this.foodPostImageRepository = foodPostImageRepository;
        this.storageService = storageService;
        this.foodPostSearchRepository = foodPostSearchRepository;
        this.userSearchRepository = userSearchRepository;
    }

    public FoodPostResponseDTO createFoodPost(
            CreateFoodPostRequestDTO request,
            String userEmail
    ) {

        User donor = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        if (!donor.getEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Email not verified. Please verify your email to post food."
            );
        }

        if (donor.getRole() != UserRoleEnum.DONOR) {
            throw new UnauthorizedActionException(
                    "Only donors can post food"
            );
        }

        FoodPost foodPost = new FoodPost();

        foodPost.setFoodName(request.getFoodName());
        foodPost.setDescription(request.getDescription());
        foodPost.setQuantity(request.getQuantity());
        foodPost.setPickupAddress(request.getPickupAddress());
        foodPost.setExpiryTime(request.getExpiryTime());

        foodPost.setDonor(donor);
        foodPost.setLatitude(request.getLatitude());
        foodPost.setLongitude(request.getLongitude());
        foodPost = foodPostRepository.save(foodPost);

        // Sync to Elasticsearch
        FoodPostDocument esDoc = new FoodPostDocument(
                String.valueOf(foodPost.getId()),
                foodPost.getFoodName(),
                foodPost.getDescription(),
                foodPost.getStatus().name(),
                new GeoPoint(foodPost.getLatitude(), foodPost.getLongitude())
        );
        foodPostSearchRepository.save(esDoc);

        // O(log n) Spatial query for nearby receivers using Elasticsearch
        List<UserDocument> nearbyReceivers = userSearchRepository.findByRoleAndLocationNear(
                UserRoleEnum.RECEIVER.name(),
                new GeoPoint(foodPost.getLatitude(), foodPost.getLongitude()),
                "50km"
        );

        for (UserDocument receiverDoc : nearbyReceivers) {
            // Verify if email is verified by fetching from MySQL, since ES doesn't have it (or we could add it to ES)
            User receiver = userRepository.findByEmail(receiverDoc.getEmail()).orElse(null);
            
            if (receiver != null && receiver.getEmailVerified()) {
                emailService.sendFoodNotificationEmail(
                        receiver.getEmail(),
                        foodPost.getFoodName(),
                        foodPost.getPickupAddress(),
                        foodPost.getQuantity()
                );
            }
        }
        
        return new FoodPostResponseDTO(
                foodPost.getId(),
                foodPost.getFoodName(),
                foodPost.getDescription(),
                foodPost.getQuantity(),
                foodPost.getPickupAddress(),
                foodPost.getLatitude(),
                foodPost.getLongitude(),
                foodPost.getExpiryTime(),
                foodPost.getDonor().getName()
        );
    }

    public Page<FoodPostResponseDTO>
    getAvailableFoodPosts(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<FoodPost> foodPosts =
                foodPostRepository.findByStatus(
                        FoodStatus.AVAILABLE,
                        pageable
                );

        return foodPosts.map(foodPost ->
                new FoodPostResponseDTO(
                        foodPost.getId(),
                        foodPost.getFoodName(),
                        foodPost.getDescription(),
                        foodPost.getQuantity(),
                        foodPost.getPickupAddress(),
                        foodPost.getLatitude(),
                        foodPost.getLongitude(),
                        foodPost.getExpiryTime(),
                        foodPost.getDonor().getName()
                )
        );
    }

    @Transactional
    public void claimFood(
        Long foodPostId,
        ClaimFoodRequestDTO request,
        String userEmail
    ) {

    User receiver = userRepository.findByEmail(userEmail)
            .orElseThrow(() ->
                    new UsernameNotFoundException(
                            "User not found"
                    ));

    if (!receiver.getEmailVerified()) {
        throw new EmailNotVerifiedException(
                "Email not verified. Please verify your email to claim food."
        );
    }

    if (receiver.getRole() != UserRoleEnum.RECEIVER) {
        throw new UnauthorizedActionException(
                "Only receivers can claim food"
        );
    }

    FoodPost foodPost = foodPostRepository.findWithLockById(foodPostId)
            .orElseThrow(() ->
                    new FoodNotFoundException("Food post not found"));

    if (foodPost.getStatus() == FoodStatus.DELETED) {
        throw new FoodNotFoundException(
                "Food post not found"
        );
    }

    if (foodPost.getStatus() == FoodStatus.CLAIMED) {
        throw new RuntimeException(
                "Food already fully claimed"
        );
    }

    Integer availableQuantity = foodPost.getQuantity();

    Integer requestedQuantity =
            request.getQuantityNeeded();

    if (requestedQuantity > availableQuantity) {
        throw new InsufficientQuantityException(
                "Requested quantity exceeds available quantity"
        );
    }

    Integer remainingQuantity =
            availableQuantity - requestedQuantity;

    foodPost.setQuantity(remainingQuantity);

    if (remainingQuantity == 0) {
        foodPost.setStatus(FoodStatus.CLAIMED);
    }

    foodPostRepository.save(foodPost);

    // Sync status change to Elasticsearch
    foodPostSearchRepository.findById(String.valueOf(foodPost.getId())).ifPresent(doc -> {
        doc.setStatus(foodPost.getStatus().name());
        doc.setQuantity(foodPost.getQuantity()); // Assuming you add quantity later if needed, but status is key
        foodPostSearchRepository.save(doc);
    });

    FoodClaim foodClaim = new FoodClaim(
            requestedQuantity,
            receiver,
            foodPost
    );
    foodClaimRepository.save(foodClaim);
    }

    public List<ClaimFoodResponseDTO>
        getMyClaims(String userEmail) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        if(user.getRole() != UserRoleEnum.RECEIVER) {
            throw new UnauthorizedActionException(
                    "Only receivers can view their claims"
            );
        }

        List<FoodClaim> claims =
                foodClaimRepository
                        .findByReceiver(user);

        return claims.stream()
                .map(claim ->
                        new ClaimFoodResponseDTO(
                                claim.getFoodPost()
                                        .getFoodName(),

                                claim.getQuantityClaimed(),

                                claim.getClaimedAt(),

                                claim.getFoodPost()
                                        .getDonor()
                                        .getName()
                        )
                )
                .toList();
        }

        public List<DonorFoodPostResponseDTO>
        getMyFoodPosts(String userEmail){
                User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(
                                () -> new RuntimeException("User not found")
                            );
                if(user.getRole() != UserRoleEnum.DONOR){
                        throw new UnauthorizedActionException(
                                "Only donors can view their food posts"
                        );
                }

                List<FoodPost> foodPosts = foodPostRepository.findByDonor(user);

                return foodPosts.stream().map(
                        foodPost -> {
                                List<ClaimFoodSummaryDTO> claims = 
                                        foodPost.getClaims().stream().map(
                                                claim -> new ClaimFoodSummaryDTO(
                        
                                                        claim.getReceiver().getName(),
                                                        claim.getQuantityClaimed()
                                                )
                                        ).toList();
                                return new DonorFoodPostResponseDTO(
                                        foodPost.getId(),
                                        foodPost.getFoodName(),
                                        foodPost.getDescription(),
                                        foodPost.getQuantity(),
                                        foodPost.getPickupAddress(),
                                        foodPost.getExpiryTime(),
                                        foodPost.getStatus(),
                                        claims
                                );
                        }
                ).toList();     
        }

        public FoodPostResponseDTO getFoodPostById(Long foodPostId) {
                FoodPost foodPost = foodPostRepository.findById(foodPostId)
                        .orElseThrow(() -> new FoodNotFoundException("Food post not found"));

                if (foodPost.getStatus() == FoodStatus.DELETED) {
                        throw new FoodNotFoundException("Food post not found");
                }

                return new FoodPostResponseDTO(
                        foodPost.getId(),
                        foodPost.getFoodName(),
                        foodPost.getDescription(),
                        foodPost.getQuantity(),
                        foodPost.getPickupAddress(),
                        foodPost.getLatitude(),
                        foodPost.getLongitude(),
                        foodPost.getExpiryTime(),
                        foodPost.getDonor().getName()
                );
        
        
        }

        @Transactional
        public FoodPostResponseDTO updateFoodPost(

                Long foodPostId,

                UpdateFoodPostRequestDTO request,

                String userEmail

        ) {

        User donor = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        FoodPost foodPost =
                foodPostRepository
                        .findWithLockById(foodPostId)
                        .orElseThrow(() ->
                                new FoodNotFoundException(
                                        "Food post not found"
                                ));

        if (foodPost.getStatus() == FoodStatus.DELETED) {
                throw new FoodNotFoundException(
                        "Food post not found"
                );
        }

        if (
                !foodPost.getDonor()
                        .getId()
                        .equals(donor.getId())
        ) {

                throw new UnauthorizedActionException(
                        "You can update only your own food posts"
                );
        }

        foodPost.setFoodName(
                request.getFoodName()
        );

        foodPost.setDescription(
                request.getDescription()
        );

        foodPost.setQuantity(
                request.getQuantity()
        );

        foodPost.setExpiryTime(
                request.getExpiryTime()
        );

        foodPost.setLatitude(
                request.getLatitude()
        );

        foodPost.setLongitude(
                request.getLongitude()
        );

        foodPost.setPickupAddress(
                request.getLocation()
        );

        FoodPost updatedFoodPost =
                foodPostRepository.save(foodPost);

        // Sync updates to Elasticsearch
        foodPostSearchRepository.findById(String.valueOf(updatedFoodPost.getId())).ifPresent(doc -> {
            doc.setFoodName(updatedFoodPost.getFoodName());
            doc.setDescription(updatedFoodPost.getDescription());
            doc.setLocation(new GeoPoint(updatedFoodPost.getLatitude(), updatedFoodPost.getLongitude()));
            foodPostSearchRepository.save(doc);
        });

        return new FoodPostResponseDTO(
                updatedFoodPost.getId(),
                updatedFoodPost.getFoodName(),
                updatedFoodPost.getDescription(),
                updatedFoodPost.getQuantity(),
                updatedFoodPost.getPickupAddress(),
                updatedFoodPost.getLatitude(),
                updatedFoodPost.getLongitude(),
                updatedFoodPost.getExpiryTime(),
                updatedFoodPost.getDonor().getName()
        );
        }

        @Transactional
        public void deleteFoodPost(Long foodPostId, String userEmail) {

        User donor = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FoodPost foodPost = foodPostRepository
                .findWithLockById(foodPostId)
                .orElseThrow(() -> new FoodNotFoundException("Food post not found"));

        if (!foodPost.getDonor().getId().equals(donor.getId())) {
            throw new UnauthorizedActionException("You can delete only your own food posts");
        }

        // Block deletion if any claim is currently in-flight
        boolean hasActiveClaims = foodClaimRepository.existsByFoodPostAndStatusIn(
                foodPost,
                List.of(ClaimStatus.ACTIVE, ClaimStatus.DONOR_CONFIRMED)
        );
        if (hasActiveClaims) {
            throw new RuntimeException(
                    "Cannot delete this post — it has active or in-progress claims. "
                    + "Cancel those claims first, or wait for them to complete."
            );
        }

        foodPost.setStatus(FoodStatus.DELETED);
        foodPostRepository.save(foodPost);

        // Remove from Elasticsearch
        foodPostSearchRepository.deleteById(String.valueOf(foodPostId));
        }

    // ── Task 3: Food post image upload ──────────────────────────────────────

    @Transactional
    public FoodPostImagesResponseDTO uploadImages(
            Long foodPostId,
            List<MultipartFile> files,
            String userEmail
    ) {
        User donor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        FoodPost foodPost = foodPostRepository.findById(foodPostId)
                .orElseThrow(() -> new FoodNotFoundException("Food post not found"));

        if (!foodPost.getDonor().getId().equals(donor.getId())) {
            throw new UnauthorizedActionException(
                    "You can only upload images to your own food posts"
            );
        }

        List<String> uploadedUrls = files.stream()
                .map(file -> {
                    String url = storageService.store(file, "food-posts");
                    FoodPostImage image = new FoodPostImage(url, foodPost);
                    foodPostImageRepository.save(image);
                    return url;
                })
                .collect(Collectors.toList());

        return new FoodPostImagesResponseDTO("Images uploaded successfully", uploadedUrls);
    }

    public FoodPostImagesResponseDTO getImages(Long foodPostId) {

        FoodPost foodPost = foodPostRepository.findById(foodPostId)
                .orElseThrow(() -> new FoodNotFoundException("Food post not found"));

        List<String> imageUrls = foodPostImageRepository
                .findByFoodPost(foodPost)
                .stream()
                .map(FoodPostImage::getImageUrl)
                .collect(Collectors.toList());

        return new FoodPostImagesResponseDTO("Images retrieved", imageUrls);
    }

    // ── Task 8: Nearby food search ───────────────────────────────────────────

    public List<NearbyFoodPostResponseDTO> getNearbyFoodPosts(
            String userEmail,
            double radiusKm,
            int page,
            int size
    ) {
        User receiver = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (receiver.getLatitude() == null || receiver.getLongitude() == null) {
            throw new RuntimeException(
                    "Your location is not set. Please update your profile with coordinates."
            );
        }

        // O(log n) spatial query via Elasticsearch BKD Trees
        Page<FoodPostDocument> esResults = foodPostSearchRepository.findByLocationNear(
                new GeoPoint(receiver.getLatitude(), receiver.getLongitude()),
                radiusKm + "km",
                PageRequest.of(page, size)
        );

        double receiverLat = receiver.getLatitude();
        double receiverLon = receiver.getLongitude();

        List<NearbyFoodPostResponseDTO> nearby = esResults.stream()
                .filter(doc -> doc.getStatus().equals(FoodStatus.AVAILABLE.name())) // Extra safety
                .map(doc -> {
                    // Fetch full entity from MySQL (or you could store more fields in ES)
                    FoodPost fp = foodPostRepository.findById(Long.valueOf(doc.getId())).orElse(null);
                    if (fp == null) return null;

                    double distanceKm = GeoUtils.calculateDistanceKm(
                            receiverLat, receiverLon,
                            fp.getLatitude(), fp.getLongitude()
                    );
                    return new NearbyFoodPostResponseDTO(
                            fp.getId(),
                            fp.getFoodName(),
                            fp.getDescription(),
                            fp.getQuantity(),
                            fp.getPickupAddress(),
                            fp.getLatitude(),
                            fp.getLongitude(),
                            fp.getExpiryTime(),
                            fp.getDonor().getName(),
                            Math.round(distanceKm * 10.0) / 10.0
                    );
                })
                .filter(dto -> dto != null)
                .sorted(Comparator.comparingDouble(NearbyFoodPostResponseDTO::getDistanceKm))
                .collect(Collectors.toList());

        return nearby;
    }

}