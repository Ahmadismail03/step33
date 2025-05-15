package com.example.enrollment.client;

import com.example.enrollment.model.Course;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "course-service")
public interface CourseClient {
    
    @GetMapping("/api/courses/{courseId}")
    Course getCourseById(@RequestHeader("Authorization") String authToken, @PathVariable Long courseId);
    
    @GetMapping("/api/courses/exists/{courseId}")
    boolean courseExists(@RequestHeader("Authorization") String authToken, @PathVariable Long courseId);
} 