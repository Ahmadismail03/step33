package com.example.enrollment.model;

public class User {
    private Long userId;
    private String name;
    private String email;
    private Role role;

    public enum Role {
        STUDENT,
        INSTRUCTOR,
        ADMIN
    }

    // Default constructor
    public User() {
    }

    // Constructor for quick user creation
    public User(Long userId, String name, String email, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isInstructor() {
        return this.role == Role.INSTRUCTOR;
    }

    public boolean isStudent() {
        return this.role == Role.STUDENT;
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