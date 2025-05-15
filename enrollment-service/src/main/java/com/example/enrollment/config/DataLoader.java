package com.example.enrollment.config;

import com.example.enrollment.model.Enrollment;
import com.example.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public DataLoader(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public void run(String... args) {
        // Only load data if the repository is empty
        if (enrollmentRepository.count() == 0) {
            loadSampleData();
        }
    }

    private void loadSampleData() {
        // Sample enrollments - Student 1 enrolled in Courses 1, 2
        Enrollment enrollment1 = new Enrollment(1L, 1L);
        enrollment1.setEnrollmentDate(LocalDateTime.now().minusDays(30));
        enrollment1.activate();
        enrollment1.updateProgress(65);
        enrollmentRepository.save(enrollment1);

        Enrollment enrollment2 = new Enrollment(1L, 2L);
        enrollment2.setEnrollmentDate(LocalDateTime.now().minusDays(15));
        enrollment2.activate();
        enrollment2.updateProgress(30);
        enrollmentRepository.save(enrollment2);

        // Student 2 enrolled in Course 1 (completed) and Course 3
        Enrollment enrollment3 = new Enrollment(2L, 1L);
        enrollment3.setEnrollmentDate(LocalDateTime.now().minusDays(60));
        enrollment3.complete();
        enrollmentRepository.save(enrollment3);

        Enrollment enrollment4 = new Enrollment(2L, 3L);
        enrollment4.setEnrollmentDate(LocalDateTime.now().minusDays(5));
        enrollment4.activate();
        enrollment4.updateProgress(10);
        enrollmentRepository.save(enrollment4);

        // Student 3 enrolled in Course 2 (dropped) and Course 3
        Enrollment enrollment5 = new Enrollment(3L, 2L);
        enrollment5.setEnrollmentDate(LocalDateTime.now().minusDays(45));
        enrollment5.drop();
        enrollmentRepository.save(enrollment5);

        Enrollment enrollment6 = new Enrollment(3L, 3L);
        enrollment6.setEnrollmentDate(LocalDateTime.now().minusDays(10));
        enrollment6.activate();
        enrollment6.updateProgress(25);
        enrollmentRepository.save(enrollment6);
    }
} 