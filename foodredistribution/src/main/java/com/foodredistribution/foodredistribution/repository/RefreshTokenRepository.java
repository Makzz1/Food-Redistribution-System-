package com.foodredistribution.foodredistribution.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodredistribution.foodredistribution.entity.RefreshToken;
import com.foodredistribution.foodredistribution.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}
