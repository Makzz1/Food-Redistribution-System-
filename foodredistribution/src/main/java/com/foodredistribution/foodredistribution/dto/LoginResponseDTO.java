package com.foodredistribution.foodredistribution.dto;

public class LoginResponseDTO {

    private String message;
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String message, Long userId, String accessToken) {
        this.message = message;
        this.userId = userId;
        this.accessToken = accessToken;
    }

    public LoginResponseDTO(String message, Long userId, String accessToken, String refreshToken, long expiresIn) {
        this.message = message;
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Kept for backward compatibility — returns the access token.
     */
    public String getToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

}

