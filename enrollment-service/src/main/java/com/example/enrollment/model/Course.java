package com.example.enrollment.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private Long courseId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private List<String> tags;
    private List<String> prerequisites;
    private User instructor;

    // Default constructor
    public Course() {
        this.tags = new ArrayList<>();
        this.prerequisites = new ArrayList<>();
    }

    // Constructor with required fields
    public Course(Long courseId, String name) {
        this.courseId = courseId;
        this.name = name;
        this.tags = new ArrayList<>();
        this.prerequisites = new ArrayList<>();
    }

    // Getters and Setters
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }
    
    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    
    public List<String> getPrerequisites() {
        return prerequisites != null ? prerequisites : new ArrayList<>();
    }
    
    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", instructor=" + instructor +
                '}';
    }
} 