package com.example.auth.payload;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String bio;
    
    private String profilePictureUrl;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
} 