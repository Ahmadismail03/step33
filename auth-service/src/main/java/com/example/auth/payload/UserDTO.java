package com.example.auth.payload;

import java.io.Serializable;

/**
 * Data Transfer Object for User information.
 * Used to send user data to clients without exposing sensitive information.
 */
public class UserDTO implements Serializable {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String profilePictureUrl;
    private String bio;

    // Default constructor
    public UserDTO() {
    }

    // Constructor from fields
    public UserDTO(Long userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Constructor with all fields
    public UserDTO(Long userId, String name, String email, String role, String profilePictureUrl, String bio) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profilePictureUrl = profilePictureUrl;
        this.bio = bio;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
} 