package com.example.soa.Model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String token;
    
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    
    private Instant expiryDate;
    
    public PasswordResetToken() {
        this.token = UUID.randomUUID().toString();
        this.expiryDate = Instant.now().plusSeconds(86400); // 24 hours
    }
    
    public PasswordResetToken(User user) {
        this();
        this.user = user;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}