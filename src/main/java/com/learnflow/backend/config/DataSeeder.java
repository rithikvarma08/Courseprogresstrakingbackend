package com.learnflow.backend.config;

import com.learnflow.backend.models.Course;
import com.learnflow.backend.models.CourseModule;
import com.learnflow.backend.models.Enrollment;
import com.learnflow.backend.models.LearningSession;
import com.learnflow.backend.models.ProgressRecord;
import com.learnflow.backend.models.Role;
import com.learnflow.backend.models.User;
import com.learnflow.backend.repositories.CourseRepository;
import com.learnflow.backend.repositories.EnrollmentRepository;
import com.learnflow.backend.repositories.LearningSessionRepository;
import com.learnflow.backend.repositories.ProgressRecordRepository;
import com.learnflow.backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRecordRepository progressRecordRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
        UserRepository userRepository,
        CourseRepository courseRepository,
        EnrollmentRepository enrollmentRepository,
        ProgressRecordRepository progressRecordRepository,
        LearningSessionRepository learningSessionRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.progressRecordRepository = progressRecordRepository;
        this.learningSessionRepository = learningSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 || courseRepository.count() > 0) {
            return;
        }

        User admin = userRepository.save(new User(
            "Aarav Admin",
            "admin@coursetrack.com",
            passwordEncoder.encode("Admin@123"),
            Role.ADMIN
        ));

        User student = userRepository.save(new User(
            "Priya Student",
            "student@coursetrack.com",
            passwordEncoder.encode("Student@123"),
            Role.USER
        ));

        User analyst = userRepository.save(new User(
            "Maya Analyst",
            "maya@coursetrack.com",
            passwordEncoder.encode("Student@123"),
            Role.USER
        ));

        Course springCourse = createCourse(
            "Java Full Stack with Spring Boot",
            "Build a production-ready learning portal with Spring Boot, REST APIs, PostgreSQL, and deployment workflows.",
            "Backend",
            "Intermediate",
            "Rahul Menon",
            24,
            "#0f766e",
            List.of(
                module(1, "Architecture Foundations", "Break the platform into controllers, services, entities, and repositories.", "Define layered architecture, package structure, and request flow for a scalable backend.", 45),
                module(2, "Secure Authentication", "Implement Spring Security with JWT-based role access.", "Protect admin and student routes with token-based authentication and custom user loading.", 60),
                module(3, "Course Management API", "Model courses, modules, enrollments, and progress tracking.", "Design entities and endpoints that power course catalogs, detail screens, and progress updates.", 75),
                module(4, "Reporting and Deployment", "Ship dashboards, analytics, and deployment-ready configuration.", "Aggregate metrics for admins and package the application for deployment.", 55)
            )
        );

        Course reactCourse = createCourse(
            "React Dashboards with Tailwind",
            "Design a rich analytics experience with responsive cards, reusable components, and progress visualizations.",
            "Frontend",
            "Beginner",
            "Nisha Kapoor",
            18,
            "#1d4ed8",
            List.of(
                module(1, "Design Language Setup", "Create a strong visual system using Tailwind tokens and component structure.", "Establish layout rhythm, color tokens, typography, and responsive patterns for the dashboard.", 30),
                module(2, "Stateful Student Views", "Load courses, session state, and profile actions with React hooks.", "Build a dashboard shell that adapts between login, student, and admin experiences.", 50),
                module(3, "Course Detail Experience", "Show module-level progress, timers, and call-to-action sections.", "Design a focused study view where learners can log time and complete modules.", 40),
                module(4, "Analytics Components", "Render activity charts and compact progress summaries.", "Turn backend metrics into charts, spotlight cards, and admin leaderboards.", 55)
            )
        );

        Course postgresCourse = createCourse(
            "PostgreSQL for Learning Platforms",
            "Use PostgreSQL effectively for relationships, analytics, and time-based reporting across learning data.",
            "Database",
            "Intermediate",
            "Ishita Roy",
            16,
            "#b45309",
            List.of(
                module(1, "Schema Planning", "Model users, courses, modules, and progress without losing reporting flexibility.", "Translate product requirements into a normalized schema that still supports analytics queries.", 35),
                module(2, "Performance Queries", "Analyze progress and completion patterns with reusable SQL thinking.", "Use joins, aggregates, and filtered queries to power dashboard metrics.", 45),
                module(3, "Data Integrity", "Protect enrollments and progress records with constraints and indexing.", "Avoid duplicate progress states and ensure referential integrity in your platform.", 35),
                module(4, "Operational Readiness", "Prepare your database for local development and production handoff.", "Configure connection settings, migrations, and reporting-friendly defaults.", 30)
            )
        );

        enrollmentRepository.save(new Enrollment(student, springCourse, LocalDate.now().minusDays(30)));
        enrollmentRepository.save(new Enrollment(student, reactCourse, LocalDate.now().minusDays(18)));
        enrollmentRepository.save(new Enrollment(analyst, springCourse, LocalDate.now().minusDays(28)));
        enrollmentRepository.save(new Enrollment(analyst, postgresCourse, LocalDate.now().minusDays(14)));

        seedStudentProgress(student, springCourse, List.of(45L, 62L, 40L, 0L), List.of(true, true, false, false));
        seedStudentProgress(student, reactCourse, List.of(28L, 35L, 15L, 0L), List.of(true, false, false, false));
        seedStudentProgress(analyst, springCourse, List.of(35L, 30L, 20L, 10L), List.of(true, false, false, false));
        seedStudentProgress(analyst, postgresCourse, List.of(25L, 44L, 0L, 0L), List.of(true, true, false, false));

        seedSessions(student, springCourse.getModules().get(0), 6, 25);
        seedSessions(student, springCourse.getModules().get(1), 4, 37);
        seedSessions(student, springCourse.getModules().get(2), 2, 40);
        seedSessions(student, reactCourse.getModules().get(0), 5, 18);
        seedSessions(student, reactCourse.getModules().get(1), 1, 35);

        seedSessions(analyst, springCourse.getModules().get(0), 6, 20);
        seedSessions(analyst, postgresCourse.getModules().get(0), 3, 25);
        seedSessions(analyst, postgresCourse.getModules().get(1), 1, 44);

        userRepository.save(admin);
    }

    private Course createCourse(
        String title,
        String description,
        String category,
        String level,
        String instructor,
        Integer durationHours,
        String heroAccent,
        List<CourseModule> modules
    ) {
        Course course = new Course(title, description, category, level, instructor, durationHours, heroAccent);
        List<CourseModule> preparedModules = new ArrayList<>();
        for (CourseModule module : modules) {
            module.setCourse(course);
            preparedModules.add(module);
        }
        course.setModules(preparedModules);
        return courseRepository.save(course);
    }

    private CourseModule module(int order, String title, String summary, String content, int estimatedMinutes) {
        return new CourseModule(title, summary, content, order, estimatedMinutes, null);
    }

    private void seedStudentProgress(User user, Course course, List<Long> minutes, List<Boolean> completedFlags) {
        for (int index = 0; index < course.getModules().size(); index++) {
            CourseModule module = course.getModules().get(index);
            long spent = minutes.get(index);
            boolean completed = completedFlags.get(index);
            if (spent == 0 && !completed) {
                continue;
            }

            ProgressRecord record = new ProgressRecord(
                user,
                module,
                completed,
                spent,
                LocalDateTime.now().minusDays(Math.max(1, course.getModules().size() - index))
            );
            if (completed) {
                record.setCompletedAt(LocalDateTime.now().minusDays(Math.max(1, 5 - index)));
            }
            progressRecordRepository.save(record);
        }
    }

    private void seedSessions(User user, CourseModule module, int daysAgo, long minutes) {
        learningSessionRepository.save(new LearningSession(
            user,
            module,
            LocalDate.now().minusDays(daysAgo),
            minutes
        ));
    }
}
