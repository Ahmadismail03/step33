package com.example.course.mapper;

import com.example.course.dto.CourseDTO;
import com.example.course.model.Course;
import com.example.course.model.User;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseDTO toCourseDTO(Course course) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseId(course.getCourseId());
        courseDTO.setName(course.getName());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setStartDate(course.getStartDate());
        courseDTO.setEndDate(course.getEndDate());
        courseDTO.setTitle(course.getTitle());
        courseDTO.setTags(course.getTags());
        courseDTO.setPrerequisites(course.getPrerequisites());
        
        // Set instructor info if available
        if (course.getInstructor() != null) {
            courseDTO.setInstructorId(course.getInstructor().getUserId());
            courseDTO.setInstructorName(course.getInstructor().getName());
            courseDTO.setInstructorEmail(course.getInstructor().getEmail());
        }
        
        return courseDTO;
    }

    public Course toCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setCourseId(courseDTO.getCourseId());
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setStartDate(courseDTO.getStartDate());
        course.setEndDate(courseDTO.getEndDate());
        course.setTitle(courseDTO.getTitle());
        course.setTags(courseDTO.getTags());
        course.setPrerequisites(courseDTO.getPrerequisites());
        
        // Set instructor if instructorId is provided
        if (courseDTO.getInstructorId() != null) {
            User instructor = new User(courseDTO.getInstructorId());
            course.setInstructor(instructor);
        }
        
        return course;
    }
} 