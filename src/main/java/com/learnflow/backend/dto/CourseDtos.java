package com.learnflow.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class CourseDtos {

    private CourseDtos() {}

    public record CourseSummary(
        Long id,
        String title,
        String category,
        String level,
        String instructor,
        Integer durationHours,
        String description,
        String heroAccent,
        Integer progressPercent,
        Integer completedModules,
        Integer totalModules,
        Integer completedVideos,
        Integer totalVideos,
        Long totalMinutesSpent,
        String nextModule
    ) {}

    public record VideoDetail(
        Long id,
        String title,
        String videoUrl,
        Integer sequenceOrder,
        boolean completed
    ) {}

    public record ModuleProgress(
        Long id,
        Integer sequenceOrder,
        String title,
        String summary,
        String content,
        Integer estimatedMinutes,
        boolean completed,
        Long timeSpentMinutes,
        LocalDateTime lastAccessed,
        LocalDateTime completedAt,
        List<VideoDetail> videos
    ) {}

    public record CourseDetail(
        Long id,
        String title,
        String category,
        String level,
        String instructor,
        Integer durationHours,
        String description,
        String heroAccent,
        Integer progressPercent,
        Integer completedModules,
        Integer totalModules,
        Long totalMinutesSpent,
        String toughestModule,
        String nextModule,
        List<ModuleProgress> modules
    ) {}

    public record ModuleRequest(
        @NotNull @Min(1) Integer sequenceOrder,
        @NotBlank String title,
        @NotBlank String summary,
        @NotBlank String content,
        @NotNull @Min(5) Integer estimatedMinutes
    ) {}

    public record CreateCourseRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String level,
        @NotBlank String instructor,
        @NotNull @Min(1) Integer durationHours,
        @NotBlank String description,
        @NotBlank String heroAccent,
        @Valid @NotEmpty List<ModuleRequest> modules
    ) {}

    public record ProgressUpdateRequest(
        @Min(0) Integer timeSpentMinutes,
        Boolean completed
    ) {}

    public record VideoCompletionRequest(
        @NotNull Long userId,
        @NotNull Long courseId,
        @NotNull Long moduleId,
        @NotNull Long videoId
    ) {}

    public record CourseProgressResponse(
        Integer totalVideos,
        Integer completedVideos,
        Integer progressPercentage
    ) {}
}
