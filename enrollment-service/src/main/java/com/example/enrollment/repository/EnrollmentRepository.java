package com.example.enrollment.repository;

import com.example.enrollment.model.Enrollment;
import com.example.enrollment.model.Enrollment.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudentId(Long studentId);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);
    
    List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
    
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    long countByCourseId(Long courseId);
    
    long countByStudentId(Long studentId);
    
    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
} 