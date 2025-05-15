package com.example.auth.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password should be at least 6 characters")
    private String newPassword;
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 