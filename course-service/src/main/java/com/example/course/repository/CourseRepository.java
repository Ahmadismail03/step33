package com.example.course.repository;

import com.example.course.model.Course;
import com.example.course.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(User instructor);
    List<Course> findByInstructorUserId(Long instructorId);
    List<Course> findByNameContainingIgnoreCase(String name);
    List<Course> findByTitleContainingIgnoreCase(String title);
} 