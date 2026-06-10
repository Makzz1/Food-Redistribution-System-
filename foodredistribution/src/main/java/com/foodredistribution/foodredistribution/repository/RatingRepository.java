package com.foodredistribution.foodredistribution.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodredistribution.foodredistribution.entity.FoodClaim;
import com.foodredistribution.foodredistribution.entity.Rating;
import com.foodredistribution.foodredistribution.entity.User;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByReviewerAndClaim(User reviewer, FoodClaim claim);

    List<Rating> findByRatedUser(User user);

    Optional<Rating> findByReviewerAndClaim(User reviewer, FoodClaim claim);
}
