package com.example.course.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.course.dto.UserDTO;

@FeignClient(name = "auth-service", path = "/api/users")
public interface UserClient {
    
    @GetMapping("/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String authToken);
    
    @GetMapping("/current")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String authToken);
} 