package com.learnflow.backend;

import com.learnflow.backend.models.*;
import com.learnflow.backend.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnrollmentRepository enrollmentRepository;

    public DataInitializer(UserRepository userRepository, 
                           CourseRepository courseRepository, PasswordEncoder passwordEncoder,
                           EnrollmentRepository enrollmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) return;

        // Create Admin
        User admin = new User();
        admin.setFullName("System Admin");
        admin.setEmail("admin@learnflow.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        // Create Student Demo
        User student = new User();
        student.setFullName("Demo Student");
        student.setEmail("student@coursetrack.com");
        student.setPassword(passwordEncoder.encode("Student@123"));
        student.setRole(Role.USER);
        userRepository.save(student);

        // Create Course: Python
        Course python = new Course("Python for Beginners", "Master Python from scratch with hands-on projects.", "Programming", "Beginner", "Dr. Sarah Johnson", 40, "indigo-500");
        
        CourseModule pModule1 = new CourseModule("Introduction to Python", "Basics of setting up Python.", "Install Python and your first script.", 1, 30, python);
        pModule1.setVideos(List.of(
            new ModuleVideo("Installing Python", "https://www.youtube.com/embed/YYXdXT2l-Gg", 1, pModule1),
            new ModuleVideo("Hello World in Python", "https://www.youtube.com/embed/vLqTf2b6GZw", 2, pModule1)
        ));

        CourseModule pModule2 = new CourseModule("Data Types and Variables", "Understanding how data works.", "Learn about integers, strings, and lists.", 2, 45, python);
        pModule2.setVideos(List.of(
            new ModuleVideo("Variables in Python", "https://www.youtube.com/embed/Z1Yd7upQsXY", 1, pModule2),
            new ModuleVideo("Python Lists and Tuples", "https://www.youtube.com/embed/W8KRzm-HUcc", 2, pModule2)
        ));

        python.setModules(List.of(pModule1, pModule2));
        courseRepository.save(python);

        // Create Course: Java
        Course javaCourse = new Course("Java Mastery", "Deep dive into Object-Oriented Programming with Java.", "Programming", "Intermediate", "John Smith", 60, "red-500");
        
        CourseModule jModule1 = new CourseModule("Java Environment Setup", "JDK and IDE configuration.", "Setting up the Java ecosystem.", 1, 40, javaCourse);
        jModule1.setVideos(List.of(
            new ModuleVideo("What is Java?", "https://www.youtube.com/embed/eIrMbAQSU34", 1, jModule1),
            new ModuleVideo("Installing JDK", "https://www.youtube.com/embed/I7Z_L6-S3fQ", 2, jModule1)
        ));

        CourseModule jModule2 = new CourseModule("Classes and Objects", "The core of Java OOP.", "Encapsulation, inheritance, and polymorphism.", 2, 90, javaCourse);
        jModule2.setVideos(List.of(
            new ModuleVideo("Intro to Classes", "https://www.youtube.com/embed/pTB0EiLXUC8", 1, jModule2),
            new ModuleVideo("Inheritance in Java", "https://www.youtube.com/embed/m9fI7254s9Q", 2, jModule2)
        ));

        javaCourse.setModules(List.of(jModule1, jModule2));
        courseRepository.save(javaCourse);

        // Enroll Student
        enrollmentRepository.save(new Enrollment(student, python, java.time.LocalDate.now()));
    }
}
