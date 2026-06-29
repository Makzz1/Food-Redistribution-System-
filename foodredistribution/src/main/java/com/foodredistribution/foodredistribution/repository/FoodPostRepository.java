package com.foodredistribution.foodredistribution.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodredistribution.foodredistribution.entity.FoodPost;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.FoodStatus;

import jakarta.persistence.LockModeType;

public interface FoodPostRepository extends JpaRepository<FoodPost, Long> {

    Page<FoodPost> findByStatus(
        FoodStatus status,
        Pageable pageable
    );

    List<FoodPost> findByDonor(User donor);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT f
        FROM FoodPost f
        WHERE f.id = :id
        """)

    Optional<FoodPost> findWithLockById(
            @Param("id") Long id
    );

    List<FoodPost>
    findByStatusAndExpiryTimeBefore(
            FoodStatus status,
            LocalDateTime time
    );

    @Query(value = """
        SELECT * FROM food_posts f
        WHERE f.status = 'AVAILABLE'
        AND ( 6371 * acos( least(1.0, greatest(-1.0, cos( radians(cast(:lat as float8)) ) * cos( radians( cast(f.latitude as float8) ) ) * cos( radians( cast(f.longitude as float8) ) - radians(cast(:lon as float8)) ) + sin( radians(cast(:lat as float8)) ) * sin( radians( cast(f.latitude as float8) ) ) )) ) ) <= cast(:distance as float8)
        ORDER BY ( 6371 * acos( least(1.0, greatest(-1.0, cos( radians(cast(:lat as float8)) ) * cos( radians( cast(f.latitude as float8) ) ) * cos( radians( cast(f.longitude as float8) ) - radians(cast(:lon as float8)) ) + sin( radians(cast(:lat as float8)) ) * sin( radians( cast(f.latitude as float8) ) ) )) ) ) ASC
        """, 
        countQuery = """
        SELECT count(*) FROM food_posts f
        WHERE f.status = 'AVAILABLE'
        AND ( 6371 * acos( least(1.0, greatest(-1.0, cos( radians(cast(:lat as float8)) ) * cos( radians( cast(f.latitude as float8) ) ) * cos( radians( cast(f.longitude as float8) ) - radians(cast(:lon as float8)) ) + sin( radians(cast(:lat as float8)) ) * sin( radians( cast(f.latitude as float8) ) ) )) ) ) <= cast(:distance as float8)
        """,
        nativeQuery = true)
    Page<FoodPost> findNearbyAvailablePosts(
        @Param("lat") Double lat, 
        @Param("lon") Double lon, 
        @Param("distance") Double distance, 
        Pageable pageable
    );

}