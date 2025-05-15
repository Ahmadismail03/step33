package com.example.course.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "title", nullable = true)
    private String title;
    
    @Column(name = "tags", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> tags = new ArrayList<>();
    
    @Column(name = "prerequisites", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> prerequisites = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "instructor_id", referencedColumnName = "userId")
    private User instructor;

    // Default constructor
    public Course() {
        this.tags = new ArrayList<>();
        this.prerequisites = new ArrayList<>();
    }

    // Constructor with required fields
    public Course(String name) {
        this.name = name;
        this.tags = new ArrayList<>();
        this.prerequisites = new ArrayList<>();
    }

    // Constructor with courseId
    public Course(Long courseId) {
        this.courseId = courseId;
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

    // Override toString
    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", prerequisites=" + prerequisites +
                ", instructor=" + instructor +
                '}';
    }

    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Course course = (Course) o;
        return Objects.equals(courseId, course.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @PrePersist
    protected void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(3);
        }
    }
} 