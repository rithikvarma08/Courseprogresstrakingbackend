package com.learnflow.backend.controllers;

import com.learnflow.backend.dto.DashboardDtos;
import com.learnflow.backend.models.User;
import com.learnflow.backend.services.CourseTrackService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CourseTrackService courseTrackService;

    public DashboardController(CourseTrackService courseTrackService) {
        this.courseTrackService = courseTrackService;
    }

    @GetMapping("/student")
    public DashboardDtos.StudentDashboardResponse getStudentDashboard(Authentication authentication) {
        return courseTrackService.getStudentDashboard((User) authentication.getPrincipal());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDtos.AdminDashboardResponse getAdminDashboard(Authentication authentication) {
        return courseTrackService.getAdminDashboard((User) authentication.getPrincipal());
    }
}
