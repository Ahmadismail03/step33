package com.example.auth.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    public enum Role {
        STUDENT,
        INSTRUCTOR,
        ADMIN
    }

    @Enumerated(EnumType.STRING)
    private Role role;

    private String profilePictureUrl;
    private String bio;

    // Default constructor
    public User() {
    }

    // Constructor for quick user creation
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String name, String email, String password, Role role, String profilePictureUrl, String bio) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePictureUrl = profilePictureUrl;
        this.bio = bio;
    }

    public void updateProfile(String bio, String profilePictureUrl) {
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters and Setters
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

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
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

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isInstructor() {
        return this.role == Role.INSTRUCTOR;
    }

    public boolean isStudent() {
        return this.role == Role.STUDENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
} 