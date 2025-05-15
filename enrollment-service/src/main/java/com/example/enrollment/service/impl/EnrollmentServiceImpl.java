package com.example.enrollment.service.impl;

import com.example.enrollment.client.CourseClient;
import com.example.enrollment.client.UserClient;
import com.example.enrollment.dto.EnrollmentRequestDto;
import com.example.enrollment.dto.EnrollmentResponseDto;
import com.example.enrollment.dto.EnrollmentUpdateDto;
import com.example.enrollment.exception.EnrollmentNotFoundException;
import com.example.enrollment.exception.ResourceNotFoundException;
import com.example.enrollment.model.Course;
import com.example.enrollment.model.Enrollment;
import com.example.enrollment.model.Enrollment.EnrollmentStatus;
import com.example.enrollment.model.User;
import com.example.enrollment.repository.EnrollmentRepository;
import com.example.enrollment.service.EnrollmentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentServiceImpl.class);
    
    private final EnrollmentRepository enrollmentRepository;
    private final UserClient userClient;
    private final CourseClient courseClient;
    
    @Autowired
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, 
                                 UserClient userClient, 
                                 CourseClient courseClient) {
        this.enrollmentRepository = enrollmentRepository;
        this.userClient = userClient;
        this.courseClient = courseClient;
    }
    
    @Override
    @Transactional
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "enrollInCourseFallback")
    public EnrollmentResponseDto enrollInCourse(EnrollmentRequestDto enrollmentRequest, String authToken) {
        logger.info("Enrolling student {} in course {}", enrollmentRequest.getStudentId(), enrollmentRequest.getCourseId());
        
        // Check if student exists
        boolean studentExists = userClient.userExists(authToken, enrollmentRequest.getStudentId());
        if (!studentExists) {
            throw new ResourceNotFoundException("Student not found with ID: " + enrollmentRequest.getStudentId());
        }
        
        // Check if course exists
        boolean courseExists = courseClient.courseExists(authToken, enrollmentRequest.getCourseId());
        if (!courseExists) {
            throw new ResourceNotFoundException("Course not found with ID: " + enrollmentRequest.getCourseId());
        }
        
        // Check if student is already enrolled in the course
        if (isStudentEnrolledInCourse(enrollmentRequest.getStudentId(), enrollmentRequest.getCourseId())) {
            throw new IllegalStateException("Student is already enrolled in this course");
        }
        
        // Create new enrollment
        Enrollment enrollment = new Enrollment(enrollmentRequest.getStudentId(), enrollmentRequest.getCourseId());
        enrollment.activate(); // Set status to ACTIVE
        
        // Save enrollment
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        // Return response with student and course details
        return createEnrollmentResponseDto(savedEnrollment, authToken);
    }
    
    @Override
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "getEnrollmentByIdFallback")
    public EnrollmentResponseDto getEnrollmentById(Long enrollmentId, String authToken) {
        logger.info("Fetching enrollment with ID: {}", enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        return createEnrollmentResponseDto(enrollment, authToken);
    }
    
    @Override
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "getEnrollmentsByStudentIdFallback")
    public List<EnrollmentResponseDto> getEnrollmentsByStudentId(Long studentId, String authToken) {
        logger.info("Fetching enrollments for student with ID: {}", studentId);
        
        // Check if student exists
        boolean studentExists = userClient.userExists(authToken, studentId);
        if (!studentExists) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        return enrollments.stream()
                .map(enrollment -> createEnrollmentResponseDto(enrollment, authToken))
                .collect(Collectors.toList());
    }
    
    @Override
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "getEnrollmentsByCourseIdFallback")
    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(Long courseId, String authToken) {
        logger.info("Fetching enrollments for course with ID: {}", courseId);
        
        // Check if course exists
        boolean courseExists = courseClient.courseExists(authToken, courseId);
        if (!courseExists) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        
        return enrollments.stream()
                .map(enrollment -> createEnrollmentResponseDto(enrollment, authToken))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "updateEnrollmentFallback")
    public EnrollmentResponseDto updateEnrollment(Long enrollmentId, EnrollmentUpdateDto updateDto, String authToken) {
        logger.info("Updating enrollment with ID: {}", enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        // Update status if provided
        if (updateDto.getStatus() != null) {
            enrollment.setStatus(updateDto.getStatus());
        }
        
        // Update progress if provided
        if (updateDto.getProgress() != null) {
            enrollment.updateProgress(updateDto.getProgress());
        }
        
        // Save updated enrollment
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        
        return createEnrollmentResponseDto(updatedEnrollment, authToken);
    }
    
    @Override
    @Transactional
    public void cancelEnrollment(Long enrollmentId, String authToken) {
        logger.info("Cancelling enrollment with ID: {}", enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        enrollment.drop();
        enrollmentRepository.save(enrollment);
    }
    
    @Override
    public long countEnrollmentsForCourse(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }
    
    @Override
    public long countActiveEnrollmentsForCourse(Long courseId) {
        return enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }
    
    @Override
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }
    
    @Override
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "getEnrollmentsByStudentAndStatusFallback")
    public List<EnrollmentResponseDto> getEnrollmentsByStudentAndStatus(Long studentId, EnrollmentStatus status, String authToken) {
        logger.info("Fetching enrollments for student {} with status {}", studentId, status);
        
        // Check if student exists
        boolean studentExists = userClient.userExists(authToken, studentId);
        if (!studentExists) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(studentId, status);
        
        return enrollments.stream()
                .map(enrollment -> createEnrollmentResponseDto(enrollment, authToken))
                .collect(Collectors.toList());
    }
    
    @Override
    @CircuitBreaker(name = "userServiceBreaker", fallbackMethod = "getEnrollmentsByCourseAndStatusFallback")
    public List<EnrollmentResponseDto> getEnrollmentsByCourseAndStatus(Long courseId, EnrollmentStatus status, String authToken) {
        logger.info("Fetching enrollments for course {} with status {}", courseId, status);
        
        // Check if course exists
        boolean courseExists = courseClient.courseExists(authToken, courseId);
        if (!courseExists) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndStatus(courseId, status);
        
        return enrollments.stream()
                .map(enrollment -> createEnrollmentResponseDto(enrollment, authToken))
                .collect(Collectors.toList());
    }
    
    // Helper method to create enrollment response DTO with student and course details
    private EnrollmentResponseDto createEnrollmentResponseDto(Enrollment enrollment, String authToken) {
        EnrollmentResponseDto responseDto = new EnrollmentResponseDto();
        responseDto.setId(enrollment.getId());
        responseDto.setEnrollmentDate(enrollment.getEnrollmentDate());
        responseDto.setStatus(enrollment.getStatus());
        responseDto.setProgress(enrollment.getProgress());
        responseDto.setCompletionDate(enrollment.getCompletionDate());
        
        try {
            // Fetch student details
            User student = userClient.getUserById(authToken, enrollment.getStudentId());
            responseDto.setStudent(student);
            
            // Fetch course details
            Course course = courseClient.getCourseById(authToken, enrollment.getCourseId());
            responseDto.setCourse(course);
        } catch (Exception e) {
            logger.error("Error fetching student or course details: {}", e.getMessage());
            // Set minimal student info
            User minimalStudent = new User();
            minimalStudent.setUserId(enrollment.getStudentId());
            responseDto.setStudent(minimalStudent);
            
            // Set minimal course info
            Course minimalCourse = new Course();
            minimalCourse.setCourseId(enrollment.getCourseId());
            responseDto.setCourse(minimalCourse);
        }
        
        return responseDto;
    }
    
    // Fallback methods for circuit breaker
    public EnrollmentResponseDto enrollInCourseFallback(EnrollmentRequestDto enrollmentRequest, String authToken, Exception e) {
        logger.error("Fallback: Error enrolling student in course: {}", e.getMessage());
        Enrollment enrollment = new Enrollment(enrollmentRequest.getStudentId(), enrollmentRequest.getCourseId());
        enrollment.setStatus(EnrollmentStatus.PENDING);
        
        EnrollmentResponseDto responseDto = new EnrollmentResponseDto();
        responseDto.setEnrollmentDate(enrollment.getEnrollmentDate());
        responseDto.setStatus(enrollment.getStatus());
        responseDto.setProgress(0);
        
        // Set minimal student info
        User minimalStudent = new User();
        minimalStudent.setUserId(enrollmentRequest.getStudentId());
        responseDto.setStudent(minimalStudent);
        
        // Set minimal course info
        Course minimalCourse = new Course();
        minimalCourse.setCourseId(enrollmentRequest.getCourseId());
        responseDto.setCourse(minimalCourse);
        
        return responseDto;
    }
    
    public EnrollmentResponseDto getEnrollmentByIdFallback(Long enrollmentId, String authToken, Exception e) {
        logger.error("Fallback: Error fetching enrollment: {}", e.getMessage());
        
        if (e instanceof EnrollmentNotFoundException) {
            throw (EnrollmentNotFoundException) e;
        }
        
        EnrollmentResponseDto responseDto = new EnrollmentResponseDto();
        responseDto.setId(enrollmentId);
        return responseDto;
    }
    
    public List<EnrollmentResponseDto> getEnrollmentsByStudentIdFallback(Long studentId, String authToken, Exception e) {
        logger.error("Fallback: Error fetching enrollments by student: {}", e.getMessage());
        return new ArrayList<>();
    }
    
    public List<EnrollmentResponseDto> getEnrollmentsByCourseIdFallback(Long courseId, String authToken, Exception e) {
        logger.error("Fallback: Error fetching enrollments by course: {}", e.getMessage());
        return new ArrayList<>();
    }
    
    public EnrollmentResponseDto updateEnrollmentFallback(Long enrollmentId, EnrollmentUpdateDto updateDto, String authToken, Exception e) {
        logger.error("Fallback: Error updating enrollment: {}", e.getMessage());
        
        if (e instanceof EnrollmentNotFoundException) {
            throw (EnrollmentNotFoundException) e;
        }
        
        EnrollmentResponseDto responseDto = new EnrollmentResponseDto();
        responseDto.setId(enrollmentId);
        responseDto.setStatus(updateDto.getStatus());
        responseDto.setProgress(updateDto.getProgress());
        return responseDto;
    }
    
    public List<EnrollmentResponseDto> getEnrollmentsByStudentAndStatusFallback(Long studentId, EnrollmentStatus status, String authToken, Exception e) {
        logger.error("Fallback: Error fetching enrollments by student and status: {}", e.getMessage());
        return new ArrayList<>();
    }
    
    public List<EnrollmentResponseDto> getEnrollmentsByCourseAndStatusFallback(Long courseId, EnrollmentStatus status, String authToken, Exception e) {
        logger.error("Fallback: Error fetching enrollments by course and status: {}", e.getMessage());
        return new ArrayList<>();
    }
} 