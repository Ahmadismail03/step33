package com.example.auth.mapper;

import com.example.auth.model.User;
import com.example.auth.payload.UserDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between User entity and UserDTO
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserDTO
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserDTO(
            user.getUserId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getProfilePictureUrl(),
            user.getBio()
        );
    }
    
    /**
     * Convert UserDTO to User entity
     */
    public User toUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        
        User user = new User();
        user.setUserId(userDTO.getUserId());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        
        if (userDTO.getRole() != null) {
            try {
                user.setRole(User.Role.valueOf(userDTO.getRole()));
            } catch (IllegalArgumentException e) {
                // Default to STUDENT if role is invalid
                user.setRole(User.Role.STUDENT);
            }
        }
        
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setBio(userDTO.getBio());
        
        return user;
    }
} 