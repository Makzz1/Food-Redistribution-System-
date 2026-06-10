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

}