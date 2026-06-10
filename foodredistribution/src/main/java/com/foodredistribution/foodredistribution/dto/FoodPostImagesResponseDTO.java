package com.foodredistribution.foodredistribution.dto;

import java.util.List;

public class FoodPostImagesResponseDTO {

    private String message;
    private List<String> images;

    public FoodPostImagesResponseDTO() {
    }

    public FoodPostImagesResponseDTO(String message, List<String> images) {
        this.message = message;
        this.images = images;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
