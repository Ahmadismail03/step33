package com.example.auth.controller;

import com.example.auth.model.PasswordResetToken;
import com.example.auth.model.RefreshToken;
import com.example.auth.model.User;
import com.example.auth.payload.ApiResponse;
import com.example.auth.payload.ForgotPasswordRequest;
import com.example.auth.payload.LoginRequest;
import com.example.auth.payload.RegisterRequest;
import com.example.auth.payload.ResetPasswordRequest;
import com.example.auth.payload.TokenRefreshRequest;
import com.example.auth.payload.TokenRefreshResponse;
import com.example.auth.security.JwtTokenProvider;
import com.example.auth.security.UserPrincipal;
import com.example.auth.service.EmailService;
import com.example.auth.service.PasswordResetService;
import com.example.auth.service.RefreshTokenService;
import com.example.auth.service.UserService;
import com.example.auth.exception.TokenRefreshException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;
    
    @Value("${app.url}")
    private String appUrl;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt received for email: {}", loginRequest.getEmail());

            // Validate request
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                logger.error("Login failed: Email is empty");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid request", "Email cannot be empty"));
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                logger.error("Login failed: Password is empty");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid request", "Password cannot be empty"));
            }

            // Check if user exists
            Optional<User> userOptional = userService.findByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                logger.error("Login failed: User not found with email: {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication failed", "User not found with email: " + loginRequest.getEmail()));
            }

            User user = userOptional.get();
            logger.info("Found user with ID: {}, Role: {}", user.getUserId(), user.getRole());

            // Custom authentication logic to handle plaintext passwords
            if (!userService.validatePassword(user, loginRequest.getPassword())) {
                logger.error("Authentication failed for user: {} - Error: Bad credentials", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication failed", "Invalid email or password"));
            }
            
            // Create authentication token with authenticated user
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
            );
            
            // Set authentication in security context
            Authentication authentication = authToken;
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT
            String jwt = tokenProvider.generateToken(authentication);
            logger.info("JWT token generated for user: {}", loginRequest.getEmail());

            // Generate refresh token
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            logger.info("Refresh token generated for user: {}", loginRequest.getEmail());

            // Save refresh token
            RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user.getUserId());
            logger.info("Refresh token saved to database for user: {}", loginRequest.getEmail());

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", jwt);
            response.put("refreshToken", refreshTokenEntity.getToken());
            response.put("tokenType", "Bearer");
            response.put("id", userPrincipal.getId());
            response.put("email", userPrincipal.getEmail());
            response.put("name", userPrincipal.getName());
            response.put("role", userPrincipal.getAuthorities().iterator().next().getAuthority());
            response.put("expiresIn", jwtExpirationInMs / 1000);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Server error", "An unexpected error occurred. Please try again later."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Validate request
        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Invalid request", "Email cannot be empty"));
        }

        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Invalid request", "Password cannot be empty"));
        }

        if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Invalid request", "Name cannot be empty"));
        }

        // Check if email is already in use
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Registration failed", "Email is already in use"));
        }

        // Create user's account
        User user = new User(
                registerRequest.getName(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getRole() != null ? registerRequest.getRole() : User.Role.STUDENT,
                registerRequest.getProfilePictureUrl(),
                registerRequest.getBio()
        );

        User result = userService.save(user);

        // Return success response
        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Create authentication token with user
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                    );
                    
                    // Generate new JWT token
                    String jwt = tokenProvider.generateToken(authToken);
                    
                    return ResponseEntity.ok(new TokenRefreshResponse(jwt, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String email = request.getEmail();
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid request", "Email cannot be empty"));
            }
            
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse(true, "If your email is registered, you will receive a password reset link."));
            }
            
            User user = userOptional.get();
            
            // Generate password reset token
            PasswordResetToken token = passwordResetService.createPasswordResetTokenForUser(user);
            
            // Send email with reset link
            String resetUrl = appUrl + "/reset-password?token=" + token.getToken();
            emailService.sendPasswordResetEmail(user, resetUrl);
            
            return ResponseEntity.ok(new ApiResponse(true, "If your email is registered, you will receive a password reset link."));
            
        } catch (MessagingException e) {
            logger.error("Error sending password reset email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Server error", "An error occurred while sending the password reset email."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest request) {
        
        try {
            boolean result = passwordResetService.validatePasswordResetToken(token);
            
            if (!result) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid token", "The password reset token is invalid or has expired."));
            }
            
            Optional<User> userOptional = passwordResetService.getUserByPasswordResetToken(token);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid token", "The password reset token is invalid or has expired."));
            }
            
            User user = userOptional.get();
            userService.updatePassword(user, request.getNewPassword());
            passwordResetService.deletePasswordResetToken(token);
            
            return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully."));
            
        } catch (Exception e) {
            logger.error("Error resetting password: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Server error", "An error occurred while resetting your password."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Optional<User> userOptional = userService.findById(userPrincipal.getId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Not found", "User not found"));
        }
        
        User user = userOptional.get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getUserId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("profilePictureUrl", user.getProfilePictureUrl());
        response.put("bio", user.getBio());
        
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", error);
        errorMap.put("message", message);
        return errorMap;
    }
} 