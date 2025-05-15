package com.example.enrollment.controller;

import com.example.enrollment.dto.EnrollmentRequestDto;
import com.example.enrollment.dto.EnrollmentResponseDto;
import com.example.enrollment.dto.EnrollmentUpdateDto;
import com.example.enrollment.model.Enrollment.EnrollmentStatus;
import com.example.enrollment.service.EnrollmentService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    
    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }
    
    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> createEnrollment(
            @Valid @RequestBody EnrollmentRequestDto enrollmentRequest,
            @RequestHeader("Authorization") String authToken) {
        EnrollmentResponseDto enrollment = enrollmentService.enrollInCourse(enrollmentRequest, authToken);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authToken) {
        EnrollmentResponseDto enrollment = enrollmentService.getEnrollmentById(id, authToken);
        return ResponseEntity.ok(enrollment);
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByStudent(
            @PathVariable Long studentId,
            @RequestHeader("Authorization") String authToken) {
        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId, authToken);
        return ResponseEntity.ok(enrollments);
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourse(
            @PathVariable Long courseId,
            @RequestHeader("Authorization") String authToken) {
        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId, authToken);
        return ResponseEntity.ok(enrollments);
    }
    
    @GetMapping("/student/{studentId}/status/{status}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByStudentAndStatus(
            @PathVariable Long studentId,
            @PathVariable EnrollmentStatus status,
            @RequestHeader("Authorization") String authToken) {
        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByStudentAndStatus(studentId, status, authToken);
        return ResponseEntity.ok(enrollments);
    }
    
    @GetMapping("/course/{courseId}/status/{status}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseAndStatus(
            @PathVariable Long courseId,
            @PathVariable EnrollmentStatus status,
            @RequestHeader("Authorization") String authToken) {
        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByCourseAndStatus(courseId, status, authToken);
        return ResponseEntity.ok(enrollments);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentUpdateDto updateDto,
            @RequestHeader("Authorization") String authToken) {
        EnrollmentResponseDto updatedEnrollment = enrollmentService.updateEnrollment(id, updateDto, authToken);
        return ResponseEntity.ok(updatedEnrollment);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelEnrollment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authToken) {
        enrollmentService.cancelEnrollment(id, authToken);
        return ResponseEntity.ok(Map.of("message", "Enrollment cancelled successfully"));
    }
    
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Map<String, Long>> getEnrollmentCountForCourse(@PathVariable Long courseId) {
        long count = enrollmentService.countEnrollmentsForCourse(courseId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @GetMapping("/course/{courseId}/active-count")
    public ResponseEntity<Map<String, Long>> getActiveEnrollmentCountForCourse(@PathVariable Long courseId) {
        long count = enrollmentService.countActiveEnrollmentsForCourse(courseId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkEnrollment(
            @RequestParam Long studentId, 
            @RequestParam Long courseId) {
        boolean isEnrolled = enrollmentService.isStudentEnrolledInCourse(studentId, courseId);
        return ResponseEntity.ok(Map.of("enrolled", isEnrolled));
    }
} 