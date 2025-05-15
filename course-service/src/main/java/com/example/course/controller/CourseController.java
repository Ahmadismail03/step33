package com.example.course.controller;

import com.example.course.dto.CourseDTO;
import com.example.course.dto.UserDTO;
import com.example.course.exception.CourseNotFoundException;
import com.example.course.mapper.CourseMapper;
import com.example.course.model.Course;
import com.example.course.model.User;
import com.example.course.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private CourseMapper courseMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO, @RequestHeader("Authorization") String authToken) {
        logger.info("Creating new course with title: {}", courseDTO.getTitle());
        
        // If name is not provided but title is, use title as name
        if ((courseDTO.getName() == null || courseDTO.getName().trim().isEmpty()) && 
            courseDTO.getTitle() != null && !courseDTO.getTitle().trim().isEmpty()) {
            courseDTO.setName(courseDTO.getTitle());
            logger.info("Using title as name for the course: {}", courseDTO.getTitle());
        }
        
        // Set default dates if not provided
        if (courseDTO.getStartDate() == null) {
            courseDTO.setStartDate(LocalDate.now());
            logger.info("Setting default start date to today");
        }
        
        if (courseDTO.getEndDate() == null) {
            // Default end date is 3 months from start date
            courseDTO.setEndDate(courseDTO.getStartDate().plusMonths(3));
            logger.info("Setting default end date to 3 months from start date");
        }
        
        // Get current authenticated user
        UserDTO currentUser = courseService.getCurrentUser(authToken);
        
        // If instructor ID is not provided in the request, set the current user as instructor
        if (courseDTO.getInstructorId() == null) {
            courseDTO.setInstructorId(currentUser.getUserId());
            logger.info("Setting instructor ID to current user: {}", currentUser.getUserId());
        } else {
            // Only ADMIN can set a different instructor
            if (!"ADMIN".equals(currentUser.getRole()) && 
                !courseDTO.getInstructorId().equals(currentUser.getUserId())) {
                logger.warn("Non-admin user trying to set different instructor");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            logger.info("Using provided instructor ID: {}", courseDTO.getInstructorId());
        }
        
        Course course = courseMapper.toCourse(courseDTO);
        Course createdCourse = courseService.createCourse(course, authToken);
        CourseDTO createdCourseDTO = courseMapper.toCourseDTO(createdCourse);
        
        // Add HATEOAS links
        addSelfLink(createdCourseDTO);
        
        logger.info("Course created successfully with ID: {}", createdCourse.getCourseId());
        return new ResponseEntity<>(createdCourseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        logger.info("Fetching all courses");
        List<Course> courses = courseService.getAllCourses();
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseMapper::toCourseDTO)
                .collect(Collectors.toList());
        
        // Add HATEOAS links to each DTO
        courseDTOs.forEach(this::addSelfLink);
        
        logger.info("Fetched {} courses", courseDTOs.size());
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/instructor")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<CourseDTO>> getInstructorCourses(@RequestHeader("Authorization") String authToken) {
        logger.info("Fetching courses for instructor");
        
        // Get the currently authenticated user
        UserDTO currentUser = courseService.getCurrentUser(authToken);
        
        List<Course> courses;
        if ("ADMIN".equals(currentUser.getRole())) {
            // Admins can see all courses
            courses = courseService.getAllCourses();
        } else {
            // Instructors see only their courses
            courses = courseService.getCoursesByInstructor(currentUser.getUserId());
        }
        
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseMapper::toCourseDTO)
                .collect(Collectors.toList());
        
        // Add HATEOAS links to each DTO
        courseDTOs.forEach(this::addSelfLink);
        
        logger.info("Fetched {} courses for instructor", courseDTOs.size());
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long courseId) {
        logger.info("Fetching course with ID: {}", courseId);
        try {
            Course course = courseService.getCourseById(courseId);
            CourseDTO courseDTO = courseMapper.toCourseDTO(course);
            
            // Add HATEOAS links
            addSelfLink(courseDTO);
            
            logger.info("Fetched course with ID: {}", courseId);
            return ResponseEntity.ok(courseDTO);
        } catch (CourseNotFoundException e) {
            logger.error("Course not found with ID: {}", courseId);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId, 
            @RequestBody CourseDTO courseDTO,
            @RequestHeader("Authorization") String authToken) {
        
        logger.info("Updating course with ID: {}", courseId);
        
        // Check if courseId is valid
        if (courseId == null || courseId <= 0) {
            logger.error("Invalid course ID: {}", courseId);
            return ResponseEntity.badRequest().build();
        }
        
        // Set the course ID from the path variable to ensure consistency
        courseDTO.setCourseId(courseId);
        
        try {
            // Check if user has permission to update this course
            UserDTO currentUser = courseService.getCurrentUser(authToken);
            Course existingCourse = courseService.getCourseById(courseId);
            
            // Only course instructor or admin can update the course
            if (!"ADMIN".equals(currentUser.getRole()) && 
                existingCourse.getInstructor() != null &&
                !existingCourse.getInstructor().getUserId().equals(currentUser.getUserId())) {
                logger.warn("User {} trying to update course they don't own", currentUser.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Course course = courseMapper.toCourse(courseDTO);
            Course updatedCourse = courseService.updateCourse(courseId, course, authToken);
            CourseDTO updatedCourseDTO = courseMapper.toCourseDTO(updatedCourse);
            
            // Add HATEOAS links
            addSelfLink(updatedCourseDTO);
            
            logger.info("Course updated successfully with ID: {}", updatedCourse.getCourseId());
            return ResponseEntity.ok(updatedCourseDTO);
        } catch (CourseNotFoundException e) {
            logger.error("Course not found with ID: {}", courseId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating course with ID: {}, Error: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        logger.info("Deleting course with ID: {}", courseId);
        
        // Check if courseId is valid
        if (courseId == null || courseId <= 0) {
            logger.error("Invalid course ID: {}", courseId);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            courseService.deleteCourse(courseId);
            logger.info("Course deleted successfully with ID: {}", courseId);
            return ResponseEntity.noContent().build();
        } catch (CourseNotFoundException e) {
            logger.error("Course not found with ID: {}", courseId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting course with ID: {}, Error: {}", courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{courseId}/assign-instructor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> assignInstructor(
            @PathVariable Long courseId, 
            @RequestParam Long instructorId,
            @RequestHeader("Authorization") String authToken) {
        
        logger.info("Assigning instructor with ID: {} to course with ID: {}", instructorId, courseId);
        
        try {
            Course course = courseService.assignInstructor(courseId, instructorId, authToken);
            CourseDTO courseDTO = courseMapper.toCourseDTO(course);
            
            // Add HATEOAS links
            addSelfLink(courseDTO);
            
            logger.info("Instructor assigned successfully to course with ID: {}", courseId);
            return ResponseEntity.ok(courseDTO);
        } catch (CourseNotFoundException e) {
            logger.error("Course not found with ID: {}", courseId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error assigning instructor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(@RequestParam String query) {
        logger.info("Searching courses with query: {}", query);
        
        try {
            List<Course> courses = courseService.searchCourses(query);
            List<CourseDTO> courseDTOs = courses.stream()
                    .map(courseMapper::toCourseDTO)
                    .collect(Collectors.toList());
            
            // Add HATEOAS links to each DTO
            courseDTOs.forEach(this::addSelfLink);
            
            logger.info("Found {} courses matching the query", courseDTOs.size());
            return ResponseEntity.ok(courseDTOs);
        } catch (Exception e) {
            logger.error("Error searching courses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Public endpoint for course listings that doesn't require authentication
    @GetMapping("/public")
    public ResponseEntity<List<CourseDTO>> getPublicCourses() {
        logger.info("Fetching public course listings");
        
        List<Course> courses = courseService.getAllCourses();
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseMapper::toCourseDTO)
                .collect(Collectors.toList());
        
        // Add HATEOAS links to each DTO
        courseDTOs.forEach(this::addSelfLink);
        
        logger.info("Fetched {} public courses", courseDTOs.size());
        return ResponseEntity.ok(courseDTOs);
    }
    
    @GetMapping("/exists/{courseId}")
    public ResponseEntity<Boolean> courseExists(@PathVariable Long courseId) {
        logger.info("Checking if course exists with ID: {}", courseId);
        boolean exists = courseService.courseExists(courseId);
        return ResponseEntity.ok(exists);
    }
    
    // Helper method to add HATEOAS self link
    private void addSelfLink(CourseDTO courseDTO) {
        try {
            courseDTO.add(linkTo(methodOn(CourseController.class)
                .getCourseById(courseDTO.getCourseId())).withSelfRel());
        } catch (Exception e) {
            logger.warn("Could not add HATEOAS link to response: {}", e.getMessage());
        }
    }
} 