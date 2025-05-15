package com.example.enrollment.dto;

import com.example.enrollment.model.Enrollment.EnrollmentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class EnrollmentUpdateDto {
    
    private EnrollmentStatus status;
    
    @Min(value = 0, message = "Progress cannot be less than 0")
    @Max(value = 100, message = "Progress cannot be more than 100")
    private Integer progress;
    
    // Default constructor
    public EnrollmentUpdateDto() {
    }
    
    // Constructor with fields
    public EnrollmentUpdateDto(EnrollmentStatus status, Integer progress) {
        this.status = status;
        this.progress = progress;
    }
    
    // Getters and Setters
    public EnrollmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
} 