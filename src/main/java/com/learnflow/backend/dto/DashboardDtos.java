package com.learnflow.backend.dto;

import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {}

    public record MetricCard(
        String label,
        String value,
        String hint
    ) {}

    public record ActivityPoint(
        String label,
        Long minutes
    ) {}

    public record StudentInsight(
        String title,
        String value,
        String description
    ) {}

    public record LearnerSpotlight(
        String fullName,
        String email,
        Long minutesSpent,
        Integer completedModules,
        Integer activeCourses
    ) {}

    public record CoursePerformance(
        Long id,
        String title,
        String category,
        Integer enrollmentCount,
        Integer moduleCount,
        Integer averageProgress,
        String bottleneckModule,
        Long totalMinutesSpent
    ) {}

    public record StudentDashboardResponse(
        AuthDtos.SessionUser user,
        String headline,
        List<MetricCard> stats,
        List<ActivityPoint> weeklyActivity,
        List<CourseDtos.CourseSummary> enrolledCourses,
        List<StudentInsight> insights
    ) {}

    public record AdminDashboardResponse(
        AuthDtos.SessionUser user,
        String headline,
        List<MetricCard> stats,
        List<ActivityPoint> weeklyEngagement,
        List<CoursePerformance> coursePerformance,
        List<LearnerSpotlight> learnerSpotlights
    ) {}
}
