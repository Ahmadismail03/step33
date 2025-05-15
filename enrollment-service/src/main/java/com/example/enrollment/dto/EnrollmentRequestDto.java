package com.example.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public class EnrollmentRequestDto {
    
    @NotNull(message = "Student ID cannot be null")
    private Long studentId;
    
    @NotNull(message = "Course ID cannot be null")
    private Long courseId;
    
    // Default constructor
    public EnrollmentRequestDto() {
    }
    
    // Constructor with required fields
    public EnrollmentRequestDto(Long studentId, Long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }
    
    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
} 