package com.example.auth.controller;

import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.User;
import com.example.auth.payload.ApiResponse;
import com.example.auth.payload.UpdateProfileRequest;
import com.example.auth.payload.UserDTO;
import com.example.auth.security.CurrentUser;
import com.example.auth.security.UserPrincipal;
import com.example.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/dto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsersDto() {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Not found", "User not found with id: " + id));
        }
        User user = userOptional.get();
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}/dto")
    public ResponseEntity<UserDTO> getUserDtoById(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        User user = userOptional.get();
        UserDTO userDTO = userMapper.toUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }
    
    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        UserDTO userDTO = userMapper.toUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id, #currentUser)")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            @CurrentUser UserPrincipal currentUser) {
        
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Not found", "User not found with id: " + id));
        }
        
        User user = userOptional.get();
        
        if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
            user.setName(updateRequest.getName());
        }
        
        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio());
        }
        
        if (updateRequest.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(updateRequest.getProfilePictureUrl());
        }
        
        User updatedUser = userService.save(user);
        return ResponseEntity.ok(userMapper.toUserDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Not found", "User not found with id: " + id));
        }
        
        userService.deleteById(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String role) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userService.findByRole(userRole);
            List<UserDTO> userDTOs = users.stream()
                    .map(userMapper::toUserDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid role: " + role);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        // Check if user exists
        if (userService.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        User user = userMapper.toUser(userDTO);
        // Set a default password that should be changed on first login
        user.setPassword("defaultPassword123");
        
        User savedUser = userService.createUser(user);
        UserDTO savedUserDTO = userMapper.toUserDTO(savedUser);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserDTO);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> userExists(@PathVariable Long id) {
        boolean exists = userService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", error);
        errorMap.put("message", message);
        return errorMap;
    }
} 