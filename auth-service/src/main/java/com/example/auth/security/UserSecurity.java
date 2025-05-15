package com.example.auth.security;

import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
    
    public boolean isCurrentUser(Long userId, UserPrincipal currentUser) {
        return currentUser != null && userId.equals(currentUser.getId());
    }
} 