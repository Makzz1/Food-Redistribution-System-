package com.foodredistribution.foodredistribution.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodredistribution.foodredistribution.entity.FoodPost;
import com.foodredistribution.foodredistribution.entity.FoodPostImage;

public interface FoodPostImageRepository extends JpaRepository<FoodPostImage, Long> {

    List<FoodPostImage> findByFoodPost(FoodPost foodPost);
}
