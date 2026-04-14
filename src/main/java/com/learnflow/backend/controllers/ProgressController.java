package com.learnflow.backend.controllers;

import com.learnflow.backend.dto.CourseDtos;
import com.learnflow.backend.models.User;
import com.learnflow.backend.services.CourseTrackService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final CourseTrackService courseTrackService;

    public ProgressController(CourseTrackService courseTrackService) {
        this.courseTrackService = courseTrackService;
    }

    @PostMapping("/modules/{moduleId}")
    public CourseDtos.ModuleProgress updateProgress(
        @PathVariable Long moduleId,
        @Valid @RequestBody CourseDtos.ProgressUpdateRequest payload,
        Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return courseTrackService.updateModuleProgress(user, moduleId, payload);
    }

    @PostMapping("/videos/{videoId}")
    public CourseDtos.ModuleProgress updateVideoProgress(
        @PathVariable Long videoId,
        @RequestParam boolean completed,
        @RequestParam(required = false) Long timeSpentMinutes,
        Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return courseTrackService.updateVideoProgress(user, videoId, completed, timeSpentMinutes);
    }

    @PostMapping("/complete")
    public CourseDtos.ModuleProgress markVideoComplete(
        @Valid @RequestBody CourseDtos.VideoCompletionRequest request
    ) {
        return courseTrackService.markVideoCompleteByUserId(request.userId(), request.videoId());
    }


    @GetMapping("/{userId}/{courseId}")
    public CourseDtos.CourseProgressResponse getCourseProgress(
        @PathVariable Long userId,
        @PathVariable Long courseId
    ) {
        return courseTrackService.getCourseProgress(userId, courseId);
    }

    @GetMapping("/user/courses-progress/{userId}")
    public List<CourseDtos.CourseSummary> getCoursesProgress(
        @PathVariable Long userId
    ) {
        return courseTrackService.getUserCoursesProgress(userId);
    }

}
