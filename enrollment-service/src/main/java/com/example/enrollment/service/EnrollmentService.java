package com.example.enrollment.service;

import com.example.enrollment.dto.EnrollmentRequestDto;
import com.example.enrollment.dto.EnrollmentResponseDto;
import com.example.enrollment.dto.EnrollmentUpdateDto;
import com.example.enrollment.model.Enrollment.EnrollmentStatus;

import java.util.List;

public interface EnrollmentService {
    
    EnrollmentResponseDto enrollInCourse(EnrollmentRequestDto enrollmentRequest, String authToken);
    
    EnrollmentResponseDto getEnrollmentById(Long enrollmentId, String authToken);
    
    List<EnrollmentResponseDto> getEnrollmentsByStudentId(Long studentId, String authToken);
    
    List<EnrollmentResponseDto> getEnrollmentsByCourseId(Long courseId, String authToken);
    
    EnrollmentResponseDto updateEnrollment(Long enrollmentId, EnrollmentUpdateDto updateDto, String authToken);
    
    void cancelEnrollment(Long enrollmentId, String authToken);
    
    long countEnrollmentsForCourse(Long courseId);
    
    long countActiveEnrollmentsForCourse(Long courseId);
    
    boolean isStudentEnrolledInCourse(Long studentId, Long courseId);
    
    List<EnrollmentResponseDto> getEnrollmentsByStudentAndStatus(Long studentId, EnrollmentStatus status, String authToken);
    
    List<EnrollmentResponseDto> getEnrollmentsByCourseAndStatus(Long courseId, EnrollmentStatus status, String authToken);
} 