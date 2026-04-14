package com.learnflow.backend.controllers;

import com.learnflow.backend.dto.CourseDtos;
import com.learnflow.backend.models.Course;
import com.learnflow.backend.models.User;
import com.learnflow.backend.services.CourseTrackService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseTrackService courseTrackService;

    public CourseController(CourseTrackService courseTrackService) {
        this.courseTrackService = courseTrackService;
    }

    @GetMapping
    public List<CourseDtos.CourseSummary> getAllCourses(Authentication authentication) {
        User user = authentication == null ? null : (User) authentication.getPrincipal();
        return courseTrackService.getCatalog(user);
    }

    @GetMapping("/my")
    public List<CourseDtos.CourseSummary> getMyCourses(Authentication authentication) {
        return courseTrackService.getMyCourses((User) authentication.getPrincipal());
    }

    @GetMapping("/{id}")
    public CourseDtos.CourseDetail getCourse(@PathVariable Long id, Authentication authentication) {
        User user = authentication == null ? null : (User) authentication.getPrincipal();
        return courseTrackService.getCourseDetail(id, user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CourseDtos.CourseDetail createCourse(@Valid @RequestBody CourseDtos.CreateCourseRequest request, Authentication authentication) {
        Course course = courseTrackService.createCourse(request);
        return courseTrackService.getCourseDetail(course.getId(), (User) authentication.getPrincipal());
    }
}
