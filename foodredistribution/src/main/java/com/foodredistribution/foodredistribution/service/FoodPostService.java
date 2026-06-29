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
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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
import com.foodredistribution.foodredistribution.utils.GeoUtils;

@Service
public class FoodPostService {

    private final FoodPostRepository foodPostRepository;
    private final UserRepository userRepository;
    private final FoodClaimRepository foodClaimRepository;
    private final EmailService emailService;
    private final FoodPostImageRepository foodPostImageRepository;
    private final StorageService storageService;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public FoodPostService(
            FoodPostRepository foodPostRepository,
            UserRepository userRepository,
            FoodClaimRepository foodClaimRepository,
            EmailService emailService,
            FoodPostImageRepository foodPostImageRepository,
            StorageService storageService,
            StringRedisTemplate redisTemplate
    ) {
        this.foodPostRepository = foodPostRepository;
        this.userRepository = userRepository;
        this.foodClaimRepository = foodClaimRepository;
        this.emailService = emailService;
        this.foodPostImageRepository = foodPostImageRepository;
        this.storageService = storageService;
        this.redisTemplate = redisTemplate;
    }

    private void clearFeedAndNearbyCaches(Long foodPostId) {
        try {
            // Use SCAN instead of KEYS to avoid blocking the Redis instance
            scanAndDelete("availablePosts::*");
            scanAndDelete("nearbyPosts::*");
            if (foodPostId != null) {
                redisTemplate.delete("foodPostDetails::" + foodPostId);
            }
        } catch (Exception e) {
            // Ignore cache delete errors — cache will expire naturally
        }
    }

    /**
     * Non-blocking SCAN + DELETE: iterates keys with a cursor instead of
     * blocking the entire Redis instance like KEYS does.
     */
    private void scanAndDelete(String pattern) {
        try {
            Set<String> keysToDelete = redisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Set<String>>) connection -> {
                    Set<String> keys = new java.util.HashSet<>();
                    var scanOptions = org.springframework.data.redis.core.ScanOptions
                            .scanOptions().match(pattern).count(100).build();
                    try (var cursor = connection.keyCommands().scan(scanOptions)) {
                        while (cursor.hasNext()) {
                            keys.add(new String(cursor.next()));
                        }
                    }
                    return keys;
                }
            );
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
            }
        } catch (Exception e) {
            // Ignore scan errors
        }
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

        // Native SQL Spatial query for nearby receivers using PostgreSQL Haversine
        List<User> nearbyReceivers = userRepository.findNearbyUsersByRole(
                UserRoleEnum.RECEIVER.name(),
                foodPost.getLatitude(), 
                foodPost.getLongitude(),
                50.0 // 50km radius
        );

        for (User receiver : nearbyReceivers) {
            if (receiver.getEmailVerified()) {
                emailService.sendFoodNotificationEmail(
                        receiver.getEmail(),
                        foodPost.getFoodName(),
                        foodPost.getPickupAddress(),
                        foodPost.getQuantity()
                );
            }
        }
        
        FoodPostResponseDTO response = new FoodPostResponseDTO(
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
        clearFeedAndNearbyCaches(null);
        return response;
    }

    public Page<FoodPostResponseDTO>
    getAvailableFoodPosts(
            int page,
            int size
    ) {

        String cacheKey = "availablePosts::" + page + "-" + size;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                Map<String, Object> map = objectMapper.readValue(cached, new TypeReference<Map<String, Object>>(){});
                List<FoodPostResponseDTO> content = objectMapper.convertValue(map.get("content"), new TypeReference<List<FoodPostResponseDTO>>(){});
                long totalElements = ((Number) map.get("totalElements")).longValue();
                int pageNumber = (int) map.get("pageNumber");
                int pageSize = (int) map.get("pageSize");
                return new PageImpl<>(content, PageRequest.of(pageNumber, pageSize), totalElements);
            }
        } catch (Exception e) {}

        Pageable pageable =
                PageRequest.of(page, size);

        Page<FoodPost> foodPosts =
                foodPostRepository.findByStatus(
                        FoodStatus.AVAILABLE,
                        pageable
                );

        Page<FoodPostResponseDTO> result = foodPosts.map(foodPost ->
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

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("content", result.getContent());
            map.put("totalElements", result.getTotalElements());
            map.put("pageNumber", result.getNumber());
            map.put("pageSize", result.getSize());
            String json = objectMapper.writeValueAsString(map);
            long baseTtl = 10 * 60; // 10 mins
            long jitter = (long) (Math.random() * 120); // 0-2 mins
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(baseTtl + jitter));
        } catch (Exception e) {}

        return result;
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

    FoodClaim foodClaim = new FoodClaim(
            requestedQuantity,
            receiver,
            foodPost
    );
    foodClaimRepository.save(foodClaim);
    clearFeedAndNearbyCaches(foodPostId);
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
                String cacheKey = "foodPostDetails::" + foodPostId;
                try {
                    String cached = redisTemplate.opsForValue().get(cacheKey);
                    if (cached != null) {
                        return objectMapper.readValue(cached, FoodPostResponseDTO.class);
                    }
                } catch (Exception e) {}

                FoodPost foodPost = foodPostRepository.findById(foodPostId)
                        .orElseThrow(() -> new FoodNotFoundException("Food post not found"));

                if (foodPost.getStatus() == FoodStatus.DELETED) {
                        throw new FoodNotFoundException("Food post not found");
                }

                FoodPostResponseDTO response = new FoodPostResponseDTO(
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
        
                try {
                    String json = objectMapper.writeValueAsString(response);
                    long baseTtl = 10 * 60; // 10 mins
                    long jitter = (long) (Math.random() * 120);
                    redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(baseTtl + jitter));
                } catch (Exception e) {}

                return response;
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

        FoodPostResponseDTO response = new FoodPostResponseDTO(
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
        clearFeedAndNearbyCaches(foodPostId);
        return response;
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
        clearFeedAndNearbyCaches(foodPostId);
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
    public Page<NearbyFoodPostResponseDTO> getNearbyFoodPosts(
            double latitude,
            double longitude,
            double radiusKm,
            int page,
            int size
    ) {
        double latRounded = Math.round(latitude * 100.0) / 100.0;
        double lonRounded = Math.round(longitude * 100.0) / 100.0;
        String cacheKey = "nearbyPosts::" + latRounded + "-" + lonRounded + "-" + radiusKm + "-" + page + "-" + size;
        
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                Map<String, Object> map = objectMapper.readValue(cached, new TypeReference<Map<String, Object>>(){});
                List<NearbyFoodPostResponseDTO> content = objectMapper.convertValue(map.get("content"), new TypeReference<List<NearbyFoodPostResponseDTO>>(){});
                long totalElements = ((Number) map.get("totalElements")).longValue();
                int pageNumber = (int) map.get("pageNumber");
                int pageSize = (int) map.get("pageSize");
                return new PageImpl<>(content, PageRequest.of(pageNumber, pageSize), totalElements);
            }
        } catch (Exception e) {
            System.err.println("Error reading nearby posts from cache:");
            e.printStackTrace();
        }

        // Native PostgreSQL geospatial query
        Page<FoodPost> dbResults = foodPostRepository.findNearbyAvailablePosts(
                latitude, 
                longitude,
                radiusKm,
                PageRequest.of(page, size)
        );

        Page<NearbyFoodPostResponseDTO> result = dbResults.map(fp -> {
            double distanceKm = GeoUtils.calculateDistanceKm(
                    latitude, longitude,
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
        });

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("content", result.getContent());
            map.put("totalElements", result.getTotalElements());
            map.put("pageNumber", result.getNumber());
            map.put("pageSize", result.getSize());
            String json = objectMapper.writeValueAsString(map);
            long baseTtl = 5 * 60; // 5 mins
            long jitter = (long) (Math.random() * 60); // 0-1 min
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(baseTtl + jitter));
        } catch (Exception e) {
            System.err.println("Error saving nearby posts to cache:");
            e.printStackTrace();
        }

        return result;
    }
}