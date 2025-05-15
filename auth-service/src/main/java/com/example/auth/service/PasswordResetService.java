package com.example.auth.service;

import com.example.auth.model.PasswordResetToken;
import com.example.auth.model.User;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    
    private static final long PASSWORD_RESET_EXPIRATION_TIME = 86400000; // 24 hours
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public PasswordResetToken createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user, Instant.now().plusMillis(PASSWORD_RESET_EXPIRATION_TIME));
        
        return passwordResetTokenRepository.save(passwordResetToken);
    }
    
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        
        if (passwordResetToken.isEmpty()) {
            return false;
        }
        
        if (passwordResetToken.get().getExpiryDate().compareTo(Instant.now()) < 0) {
            passwordResetTokenRepository.delete(passwordResetToken.get());
            return false;
        }
        
        return true;
    }
    
    public Optional<User> getUserByPasswordResetToken(String token) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        
        if (passwordResetToken.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(passwordResetToken.get().getUser());
    }
    
    @Transactional
    public void deletePasswordResetToken(String token) {
        passwordResetTokenRepository.findByToken(token).ifPresent(passwordResetTokenRepository::delete);
    }
} 