package com.foodredistribution.foodredistribution.dto;

import com.foodredistribution.foodredistribution.enums.UserRoleEnum;

public class ProfileResponseDTO {

    private String name;
    private String email;
    private String phoneNumber;
    private String location;
    private UserRoleEnum role;
    private boolean phoneVerified;
    private boolean emailVerified;
    private Double rating;

    public ProfileResponseDTO() {
    }

    public ProfileResponseDTO(
            String name,
            String email,
            String phoneNumber,
            String location,
            UserRoleEnum role,
            boolean phoneVerified,
            boolean emailVerified,
            Double rating
    ) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.role = role;
        this.phoneVerified = phoneVerified;
        this.emailVerified = emailVerified;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public UserRoleEnum getRole() {
        return role;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Double getRating() {
        return rating;
    }

}

