package com.foodredistribution.foodredistribution.entity;

import com.foodredistribution.foodredistribution.enums.UserRoleEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRoleEnum role;
    private String phoneNumber;
    private boolean phoneVerified;
    private Double latitude;
    private Double longitude;
    private String location;
    private boolean emailVerified;
    private String verificationToken;
    private Double rating = 5.0;
    private Long ratingSum = 0L;
    private Long totalRatings = 0L;
    private String passwordResetToken;
    private java.time.LocalDateTime passwordResetTokenExpiry;

    // Trust metrics (raw — trust score calculated dynamically)
    private Long successfulDonations = 0L;
    private Long successfulPickups   = 0L;
    private Long noShowCount         = 0L;
    private Long reportCount         = 0L;
    private Long disputeCount        = 0L;
    private boolean banned           = false;

    public User() {
    }


    public User(String name,
         String email,
          String password,
           UserRoleEnum role,
            String phoneNumber,
             Double latitude,
             Double longitude,
             String location) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.phoneVerified = false;
        this.emailVerified = false;
        this.verificationToken = null;
        this.location = location;
        this.rating = 5.0;
    }

    public Long getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRoleEnum getRole() {
        return role;
    }

    public void setRole(UserRoleEnum role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }

    public boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public java.time.LocalDateTime getPasswordResetTokenExpiry() {
        return passwordResetTokenExpiry;
    }

    public void setPasswordResetTokenExpiry(java.time.LocalDateTime passwordResetTokenExpiry) {
        this.passwordResetTokenExpiry = passwordResetTokenExpiry;
    }

    public Long getRatingSum() { return ratingSum; }
    public void setRatingSum(Long ratingSum) { this.ratingSum = ratingSum; }

    public Long getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

    public Long getSuccessfulDonations() { return successfulDonations; }
    public void setSuccessfulDonations(Long successfulDonations) { this.successfulDonations = successfulDonations; }

    public Long getSuccessfulPickups() { return successfulPickups; }
    public void setSuccessfulPickups(Long successfulPickups) { this.successfulPickups = successfulPickups; }

    public Long getNoShowCount() { return noShowCount; }
    public void setNoShowCount(Long noShowCount) { this.noShowCount = noShowCount; }

    public Long getReportCount() { return reportCount; }
    public void setReportCount(Long reportCount) { this.reportCount = reportCount; }

    public Long getDisputeCount() { return disputeCount; }
    public void setDisputeCount(Long disputeCount) { this.disputeCount = disputeCount; }

    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }

}