package com.example.enrollment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;
    
    @Column(name = "progress")
    private Integer progress;
    
    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    // Enum for enrollment status
    public enum EnrollmentStatus {
        PENDING,
        ACTIVE,
        COMPLETED,
        DROPPED
    }
    
    // Default constructor
    public Enrollment() {
        this.enrollmentDate = LocalDateTime.now();
        this.status = EnrollmentStatus.PENDING;
        this.progress = 0;
    }
    
    // Constructor with required fields
    public Enrollment(Long studentId, Long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = LocalDateTime.now();
        this.status = EnrollmentStatus.PENDING;
        this.progress = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

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

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }
    
    // Utility methods
    public void activate() {
        this.status = EnrollmentStatus.ACTIVE;
    }
    
    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
        this.progress = 100;
        this.completionDate = LocalDateTime.now();
    }
    
    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }
    
    public void updateProgress(int newProgress) {
        this.progress = Math.min(100, Math.max(0, newProgress));
        if (this.progress == 100) {
            this.complete();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", status=" + status +
                ", progress=" + progress +
                '}';
    }
} 