package com.example.enrollment.client;

import com.example.enrollment.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface UserClient {
    
    @GetMapping("/api/users/{userId}")
    User getUserById(@RequestHeader("Authorization") String authToken, @PathVariable Long userId);
    
    @GetMapping("/api/users/exists/{userId}")
    boolean userExists(@RequestHeader("Authorization") String authToken, @PathVariable Long userId);
} 