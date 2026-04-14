package com.learnflow.backend.controllers;

import com.learnflow.backend.models.Course;
import com.learnflow.backend.models.Enrollment;
import com.learnflow.backend.models.User;
import com.learnflow.backend.repositories.CourseRepository;
import com.learnflow.backend.repositories.EnrollmentRepository;
import com.learnflow.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enroll")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{courseId}")
    public ResponseEntity<?> enrollInCourse(@PathVariable Long courseId, Authentication authentication) {
        User userDetails = (User) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            return ResponseEntity.badRequest().body("Already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment(user, course, LocalDate.now());
        enrollmentRepository.save(enrollment);

        return ResponseEntity.ok("Successfully enrolled in course");
    }

    @GetMapping
    public List<Course> getMyEnrolledCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        return enrollments.stream().map(Enrollment::getCourse).collect(Collectors.toList());
    }
}
