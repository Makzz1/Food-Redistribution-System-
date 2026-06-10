package com.foodredistribution.foodredistribution.dto;


public class RegisterResponseDTO {
    private Long id;
    private String message;

    public RegisterResponseDTO() {
    }

    public RegisterResponseDTO(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Long getId() {
        return id;
    }

    
}
