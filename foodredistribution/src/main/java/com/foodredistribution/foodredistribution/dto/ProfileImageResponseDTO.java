package com.foodredistribution.foodredistribution.dto;

public class ProfileImageResponseDTO {

    private String message;
    private String imageUrl;

    public ProfileImageResponseDTO() {
    }

    public ProfileImageResponseDTO(String message, String imageUrl) {
        this.message = message;
        this.imageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
