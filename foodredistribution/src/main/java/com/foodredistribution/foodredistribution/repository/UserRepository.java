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

        @org.springframework.data.jpa.repository.Query(value = """
            SELECT * FROM users u
            WHERE u.role = CAST(:role AS VARCHAR)
            AND ( 6371 * acos( least(1.0, greatest(-1.0, cos( radians(cast(:lat as float8)) ) * cos( radians( cast(u.latitude as float8) ) ) * cos( radians( cast(u.longitude as float8) ) - radians(cast(:lon as float8)) ) + sin( radians(cast(:lat as float8)) ) * sin( radians( cast(u.latitude as float8) ) ) )) ) ) <= cast(:distance as float8)
            """, nativeQuery = true)
        List<User> findNearbyUsersByRole(
            @org.springframework.data.repository.query.Param("role") String role, 
            @org.springframework.data.repository.query.Param("lat") Double lat, 
            @org.springframework.data.repository.query.Param("lon") Double lon, 
            @org.springframework.data.repository.query.Param("distance") Double distance
        );
}

