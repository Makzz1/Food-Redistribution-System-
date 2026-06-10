package com.foodredistribution.foodredistribution.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodredistribution.foodredistribution.entity.ProfileImage;
import com.foodredistribution.foodredistribution.entity.User;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    Optional<ProfileImage> findByUser(User user);
}
