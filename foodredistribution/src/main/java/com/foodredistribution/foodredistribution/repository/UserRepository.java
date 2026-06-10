package com.foodredistribution.foodredistribution.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.UserRoleEnum;

public interface UserRepository 
        extends JpaRepository<User, Long> {

        boolean existsByEmail(String email);
        Optional<User> findByEmail(String email);
        Optional<User> findByVerificationToken(String token);
        Optional<User> findByPasswordResetToken(String token);
        List<User> findByRoleAndEmailVerified(
                UserRoleEnum role,
                Boolean emailVerified
        );
}

