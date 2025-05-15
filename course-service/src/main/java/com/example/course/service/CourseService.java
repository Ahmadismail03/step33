package com.example.course.service;

import com.example.course.client.UserClient;
import com.example.course.dto.UserDTO;
import com.example.course.exception.CourseNotFoundException;
import com.example.course.exception.UserNotFoundException;
import com.example.course.model.Course;
import com.example.course.model.User;
import com.example.course.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserClient userClient;

    /**
     * Create a new course with the given instructor
     */
    public Course createCourse(Course course, String authToken) {
        logger.info("Creating a new course: {}", course.getTitle());
        
        // If instructor ID is provided, verify the instructor exists
        if (course.getInstructor() != null && course.getInstructor().getUserId() != null) {
            try {
                UserDTO instructor = userClient.getUserById(course.getInstructor().getUserId(), authToken);
                // Update user information from Auth Service
                User instructorEntity = course.getInstructor();
                instructorEntity.setEmail(instructor.getEmail());
                instructorEntity.setName(instructor.getName());
                
                // Convert role string to enum
                try {
                    instructorEntity.setRole(User.Role.valueOf(instructor.getRole()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid role received from Auth Service: {}", instructor.getRole());
                    instructorEntity.setRole(User.Role.INSTRUCTOR); // Default to INSTRUCTOR
                }
                
                course.setInstructor(instructorEntity);
            } catch (Exception e) {
                logger.error("Error fetching instructor details from Auth Service", e);
                throw new UserNotFoundException("Instructor not found with ID: " + course.getInstructor().getUserId());
            }
        }
        
        return courseRepository.save(course);
    }

    /**
     * Get all courses
     */
    public List<Course> getAllCourses() {
        logger.info("Fetching all courses");
        return courseRepository.findAll();
    }

    /**
     * Get a course by ID
     */
    public Course getCourseById(Long courseId) {
        logger.info("Fetching course with ID: {}", courseId);
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));
    }

    /**
     * Update an existing course
     */
    public Course updateCourse(Long courseId, Course course, String authToken) {
        logger.info("Updating course with ID: {}", courseId);
        
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));
        
        existingCourse.setTitle(course.getTitle());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setName(course.getName());
        existingCourse.setStartDate(course.getStartDate());
        existingCourse.setEndDate(course.getEndDate());
        
        // Update tags if provided
        if (course.getTags() != null) {
            existingCourse.setTags(course.getTags());
        }
        
        // Update prerequisites if provided
        if (course.getPrerequisites() != null) {
            existingCourse.setPrerequisites(course.getPrerequisites());
        }
        
        // Handle instructor update
        if (course.getInstructor() != null && course.getInstructor().getUserId() != null) {
            try {
                UserDTO instructor = userClient.getUserById(course.getInstructor().getUserId(), authToken);
                // Update user information from Auth Service
                User instructorEntity = new User(instructor.getUserId());
                instructorEntity.setEmail(instructor.getEmail());
                instructorEntity.setName(instructor.getName());
                
                // Convert role string to enum
                try {
                    instructorEntity.setRole(User.Role.valueOf(instructor.getRole()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid role received from Auth Service: {}", instructor.getRole());
                    instructorEntity.setRole(User.Role.INSTRUCTOR); // Default to INSTRUCTOR
                }
                
                existingCourse.setInstructor(instructorEntity);
            } catch (Exception e) {
                logger.error("Error fetching instructor details from Auth Service", e);
                throw new UserNotFoundException("Instructor not found with ID: " + course.getInstructor().getUserId());
            }
        }
        
        return courseRepository.save(existingCourse);
    }

    /**
     * Delete a course
     */
    public void deleteCourse(Long courseId) {
        logger.info("Deleting course with ID: {}", courseId);
        
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));
                
        courseRepository.delete(existingCourse);
    }

    /**
     * Assign an instructor to a course
     */
    public Course assignInstructor(Long courseId, Long instructorId, String authToken) {
        logger.info("Assigning instructor with ID: {} to course with ID: {}", instructorId, courseId);
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + courseId));
        
        try {
            UserDTO instructor = userClient.getUserById(instructorId, authToken);
            // Update user information from Auth Service
            User instructorEntity = new User(instructor.getUserId());
            instructorEntity.setEmail(instructor.getEmail());
            instructorEntity.setName(instructor.getName());
            
            // Convert role string to enum
            try {
                instructorEntity.setRole(User.Role.valueOf(instructor.getRole()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role received from Auth Service: {}", instructor.getRole());
                instructorEntity.setRole(User.Role.INSTRUCTOR); // Default to INSTRUCTOR
            }
            
            course.setInstructor(instructorEntity);
        } catch (Exception e) {
            logger.error("Error fetching instructor details from Auth Service", e);
            throw new UserNotFoundException("Instructor not found with ID: " + instructorId);
        }
        
        return courseRepository.save(course);
    }
    
    /**
     * Get courses by instructor ID
     */
    public List<Course> getCoursesByInstructor(Long instructorId) {
        logger.info("Fetching courses for instructor with ID: {}", instructorId);
        return courseRepository.findByInstructorUserId(instructorId);
    }
    
    /**
     * Search courses by name or title
     */
    public List<Course> searchCourses(String query) {
        logger.info("Searching courses with query: {}", query);
        List<Course> nameResults = courseRepository.findByNameContainingIgnoreCase(query);
        List<Course> titleResults = courseRepository.findByTitleContainingIgnoreCase(query);
        
        // Combine and remove duplicates
        nameResults.removeAll(titleResults);
        nameResults.addAll(titleResults);
        
        return nameResults;
    }
    
    /**
     * Get the currently authenticated user from Auth Service
     */
    public UserDTO getCurrentUser(String authToken) {
        logger.info("Fetching current user details from Auth Service");
        try {
            return userClient.getCurrentUser(authToken);
        } catch (Exception e) {
            logger.error("Error fetching current user details from Auth Service", e);
            throw new RuntimeException("Error fetching current user details", e);
        }
    }

    /**
     * Check if a course exists by ID
     */
    public boolean courseExists(Long courseId) {
        logger.info("Checking if course exists with ID: {}", courseId);
        return courseRepository.existsById(courseId);
    }
} 