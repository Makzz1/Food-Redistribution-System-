package com.foodredistribution.foodredistribution.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    public ForgotPasswordRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
