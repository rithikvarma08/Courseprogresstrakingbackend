package com.learnflow.backend.services;

import com.learnflow.backend.dto.AuthDtos;
import com.learnflow.backend.dto.CourseDtos;
import com.learnflow.backend.dto.DashboardDtos;
import com.learnflow.backend.models.Course;
import com.learnflow.backend.models.CourseModule;
import com.learnflow.backend.models.Enrollment;
import com.learnflow.backend.models.LearningSession;
import com.learnflow.backend.models.ProgressRecord;
import com.learnflow.backend.models.Role;
import com.learnflow.backend.models.User;
import com.learnflow.backend.models.ModuleVideo;
import com.learnflow.backend.models.UserProgress;
import com.learnflow.backend.repositories.UserProgressRepository;
import com.learnflow.backend.repositories.CourseModuleRepository;
import com.learnflow.backend.repositories.CourseRepository;
import com.learnflow.backend.repositories.EnrollmentRepository;
import com.learnflow.backend.repositories.LearningSessionRepository;
import com.learnflow.backend.repositories.ProgressRecordRepository;
import com.learnflow.backend.repositories.UserRepository;
import com.learnflow.backend.repositories.ModuleVideoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class CourseTrackService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRecordRepository progressRecordRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final UserRepository userRepository;
    private final ModuleVideoRepository moduleVideoRepository;
    private final UserProgressRepository userProgressRepository;

    public CourseTrackService(
        CourseRepository courseRepository,
        CourseModuleRepository courseModuleRepository,
        EnrollmentRepository enrollmentRepository,
        ProgressRecordRepository progressRecordRepository,
        LearningSessionRepository learningSessionRepository,
        UserRepository userRepository,
        ModuleVideoRepository moduleVideoRepository,
        UserProgressRepository userProgressRepository
    ) {
        this.courseRepository = courseRepository;
        this.courseModuleRepository = courseModuleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.progressRecordRepository = progressRecordRepository;
        this.learningSessionRepository = learningSessionRepository;
        this.userRepository = userRepository;
        this.moduleVideoRepository = moduleVideoRepository;
        this.userProgressRepository = userProgressRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseDtos.CourseSummary> getMyCourses(User user) {
        return enrollmentRepository.findByUserId(user.getId()).stream()
            .map(enrollment -> buildCourseSummary(enrollment.getCourse(), user.getId(), getProgressMap(user.getId(), enrollment.getCourse().getId())))
            .sorted(Comparator.comparing(CourseDtos.CourseSummary::progressPercent).reversed())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseDtos.CourseSummary> getCatalog(User user) {
        return courseRepository.findAll().stream()
            .map(course -> {
                Map<Long, ProgressRecord> progressMap = user == null
                    ? Map.of()
                    : getProgressMap(user.getId(), course.getId());
                return buildCourseSummary(course, user != null ? user.getId() : null, progressMap);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public CourseDtos.CourseDetail getCourseDetail(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Course not found"));

        if (user != null && user.getRole() == Role.USER && !enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not enrolled in this course");
        }

        Map<Long, ProgressRecord> progressMap = user == null ? Map.of() : getProgressMap(user.getId(), courseId);
        CourseProgressStats stats = calculateCourseProgress(course, user != null ? user.getId() : null, progressMap);
        List<CourseDtos.ModuleProgress> modules = orderedModules(course).stream()
            .map(module -> buildModuleProgress(user, module, progressMap.get(module.getId())))
            .toList();

        return new CourseDtos.CourseDetail(
            course.getId(),
            course.getTitle(),
            course.getCategory(),
            course.getLevel(),
            course.getInstructor(),
            course.getDurationHours(),
            course.getDescription(),
            course.getHeroAccent(),
            stats.progressPercent(),
            stats.completedModules(),
            stats.totalModules(),
            stats.totalMinutesSpent(),
            stats.toughestModule(),
            stats.nextModule(),
            modules
        );
    }

    public Course createCourse(CourseDtos.CreateCourseRequest request) {
        Course course = new Course(
            request.title(),
            request.description(),
            request.category(),
            request.level(),
            request.instructor(),
            request.durationHours(),
            request.heroAccent()
        );

        List<CourseModule> modules = new ArrayList<>();
        for (CourseDtos.ModuleRequest moduleRequest : request.modules()) {
            modules.add(new CourseModule(
                moduleRequest.title(),
                moduleRequest.summary(),
                moduleRequest.content(),
                moduleRequest.sequenceOrder(),
                moduleRequest.estimatedMinutes(),
                course
            ));
        }

        course.setModules(modules);
        return courseRepository.save(course);
    }

    public CourseDtos.ModuleProgress updateModuleProgress(User user, Long moduleId, CourseDtos.ProgressUpdateRequest request) {
        CourseModule module = courseModuleRepository.findById(moduleId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Module not found"));

        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), module.getCourse().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "You are not enrolled in this course");
        }

        ProgressRecord record = progressRecordRepository.findByUserIdAndModuleId(user.getId(), moduleId)
            .orElseGet(() -> new ProgressRecord(user, module, false, 0L, LocalDateTime.now()));

        long minutesToAdd = Optional.ofNullable(request.timeSpentMinutes()).orElse(0);
        if (minutesToAdd > 0) {
            record.setTimeSpentMinutes(Objects.requireNonNullElse(record.getTimeSpentMinutes(), 0L) + minutesToAdd);
            learningSessionRepository.save(new LearningSession(user, module, LocalDate.now(), minutesToAdd));
        }

        if (Boolean.TRUE.equals(request.completed()) && !record.isCompleted()) {
            record.setCompleted(true);
            record.setCompletedAt(LocalDateTime.now());
        } else if (Boolean.FALSE.equals(request.completed())) {
            record.setCompleted(false);
            record.setCompletedAt(null);
        }

        record.setLastAccessed(LocalDateTime.now());
        ProgressRecord savedRecord = progressRecordRepository.save(record);

        // SYNC FIX: If module is marked complete, mark all associated videos as complete too
        if (Boolean.TRUE.equals(request.completed())) {
            List<ModuleVideo> videos = moduleVideoRepository.findByModuleIdOrderBySequenceOrderAsc(moduleId);
            for (ModuleVideo video : videos) {
                UserProgress vp = userProgressRepository.findByUserIdAndVideoId(user.getId(), video.getId())
                    .orElseGet(() -> new UserProgress(user, module.getCourse(), module, video, false));
                if (!vp.isCompleted()) {
                    vp.setCompleted(true);
                    userProgressRepository.save(vp);
                }
            }
        }

        return buildModuleProgress(user, module, savedRecord);
    }

    public CourseDtos.ModuleProgress updateVideoProgress(User user, Long videoId, boolean completed, Long timeSpentMinutes) {
        ModuleVideo video = moduleVideoRepository.findById(videoId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Video not found"));

        UserProgress progress = userProgressRepository.findByUserIdAndVideoId(user.getId(), videoId)
            .orElseGet(() -> new UserProgress(user, video.getModule().getCourse(), video.getModule(), video, false));

        progress.setCompleted(completed);
        userProgressRepository.save(progress);

        // Check if all videos in this module are completed
        CourseModule module = video.getModule();
        List<ModuleVideo> moduleVideos = moduleVideoRepository.findByModuleIdOrderBySequenceOrderAsc(module.getId());
        List<UserProgress> userModuleProgress = userProgressRepository.findByUserIdAndModuleId(user.getId(), module.getId());

        
        long completedCount = userModuleProgress.stream().filter(UserProgress::isCompleted).count();
        int timeToLog = timeSpentMinutes != null ? timeSpentMinutes.intValue() : 0;
        
        if (completedCount == moduleVideos.size() && !moduleVideos.isEmpty()) {
            // Auto-complete the module when all its videos are done
            updateModuleProgress(user, module.getId(), new CourseDtos.ProgressUpdateRequest(timeToLog, true));
        } else {
            // Auto-uncomplete if they uncheck a video
            updateModuleProgress(user, module.getId(), new CourseDtos.ProgressUpdateRequest(timeToLog, false));
        }


        ProgressRecord moduleRecord = progressRecordRepository.findByUserIdAndModuleId(user.getId(), module.getId()).orElse(null);
        return buildModuleProgress(user, module, moduleRecord);
    }

    public CourseDtos.CourseProgressResponse getCourseProgress(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Course not found"));
        
        Map<Long, ProgressRecord> progressMap = getProgressMap(userId, courseId);
        CourseProgressStats stats = calculateCourseProgress(course, userId, progressMap);
        
        return new CourseDtos.CourseProgressResponse(
            stats.totalVideos(),
            stats.completedVideos(),
            stats.progressPercent()
        );
    }

    public CourseDtos.ModuleProgress markVideoCompleteByUserId(Long userId, Long videoId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return updateVideoProgress(user, videoId, true, null);
    }

    public List<CourseDtos.CourseSummary> getUserCoursesProgress(Long userId) {

        User user = userRepository.findById(userId).orElseThrow();
        return enrollmentRepository.findByUserId(userId).stream()
            .map(enrollment -> buildCourseSummary(enrollment.getCourse(), userId, getProgressMap(userId, enrollment.getCourse().getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public DashboardDtos.StudentDashboardResponse getStudentDashboard(User user) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        List<CourseDtos.CourseSummary> courses = enrollments.stream()
            .map(enrollment -> buildCourseSummary(enrollment.getCourse(), user.getId(), getProgressMap(user.getId(), enrollment.getCourse().getId())))
            .sorted(Comparator.comparing(CourseDtos.CourseSummary::progressPercent).reversed())
            .toList();

        long totalMinutes = courses.stream().mapToLong(CourseDtos.CourseSummary::totalMinutesSpent).sum();
        long completedCourses = courses.stream().filter(course -> course.progressPercent() == 100).count();
        long averageProgress = courses.isEmpty()
            ? 0
            : Math.round(courses.stream().mapToInt(CourseDtos.CourseSummary::progressPercent).average().orElse(0));

        List<LearningSession> weeklySessions = learningSessionRepository.findByUserIdAndSessionDateBetween(
            user.getId(),
            LocalDate.now().minusDays(6),
            LocalDate.now()
        );
        long weeklyMinutes = weeklySessions.stream().mapToLong(LearningSession::getMinutesSpent).sum();

        String focusCourseTitle = courses.isEmpty() ? "your first course" : courses.get(0).title();
        String headline = courses.isEmpty()
            ? "Start by enrolling in a course to unlock your progress dashboard."
            : "You are building momentum in " + focusCourseTitle + " with " + weeklyMinutes + " minutes logged this week.";

        String toughestModule = courses.stream()
            .map(course -> getCourseDetail(course.id(), user))
            .map(CourseDtos.CourseDetail::toughestModule)
            .filter(Objects::nonNull)
            .filter(value -> !value.isBlank())
            .findFirst()
            .orElse("No module data yet");

        String nextModule = courses.stream()
            .map(CourseDtos.CourseSummary::nextModule)
            .filter(Objects::nonNull)
            .filter(value -> !value.equals("Course completed"))
            .findFirst()
            .orElse("You are ready to finish your last remaining module.");

        List<DashboardDtos.MetricCard> stats = List.of(
            new DashboardDtos.MetricCard("Enrolled Courses", String.valueOf(courses.size()), "Active learning tracks"),
            new DashboardDtos.MetricCard("Completed Courses", String.valueOf(completedCourses), "Courses finished end-to-end"),
            new DashboardDtos.MetricCard("Learning Hours", formatHours(totalMinutes), "Tracked module time"),
            new DashboardDtos.MetricCard("Average Progress", averageProgress + "%", "Across your enrolled courses")
        );

        List<DashboardDtos.StudentInsight> insights = List.of(
            new DashboardDtos.StudentInsight("Focus Module", toughestModule, "The module that consumed the most learning time so far."),
            new DashboardDtos.StudentInsight("Next Best Step", nextModule, "The recommended module to keep your pace steady."),
            new DashboardDtos.StudentInsight("This Week", weeklyMinutes + " mins", "Time logged over the last 7 days.")
        );

        return new DashboardDtos.StudentDashboardResponse(
            toSessionUser(user),
            headline,
            stats,
            buildActivityPoints(weeklySessions),
            courses,
            insights
        );
    }

    @Transactional(readOnly = true)
    public DashboardDtos.AdminDashboardResponse getAdminDashboard(User admin) {
        List<User> learners = userRepository.findByRole(Role.USER);
        List<Course> courses = courseRepository.findAll();
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        List<LearningSession> weeklySessions = learningSessionRepository.findBySessionDateBetween(LocalDate.now().minusDays(6), LocalDate.now());

        List<DashboardDtos.CoursePerformance> performance = courses.stream()
            .map(this::buildCoursePerformance)
            .sorted(Comparator.comparing(DashboardDtos.CoursePerformance::averageProgress).reversed())
            .toList();

        long totalLearningMinutes = progressRecordRepository.findAll().stream()
            .map(ProgressRecord::getTimeSpentMinutes)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .sum();

        long completedTracks = allEnrollments.stream()
            .filter(enrollment -> calculateCourseProgress(
                enrollment.getCourse(),
                enrollment.getUser().getId(),
                getProgressMap(enrollment.getUser().getId(), enrollment.getCourse().getId())
            ).progressPercent() == 100)
            .count();

        long completionRate = allEnrollments.isEmpty()
            ? 0
            : Math.round((completedTracks * 100.0) / allEnrollments.size());

        List<DashboardDtos.MetricCard> stats = List.of(
            new DashboardDtos.MetricCard("Learners", String.valueOf(learners.size()), "Registered student accounts"),
            new DashboardDtos.MetricCard("Courses", String.valueOf(courses.size()), "Published learning tracks"),
            new DashboardDtos.MetricCard("Modules", String.valueOf(courseModuleRepository.count()), "Structured learning steps"),
            new DashboardDtos.MetricCard("Completion Rate", completionRate + "%", "Enrollments reaching 100%")
        );

        List<DashboardDtos.LearnerSpotlight> learnerSpotlights = learners.stream()
            .map(this::buildLearnerSpotlight)
            .sorted(Comparator.comparing(DashboardDtos.LearnerSpotlight::minutesSpent).reversed())
            .limit(5)
            .toList();

        String headline = performance.isEmpty()
            ? "Create your first course to start tracking enrollments and learning behavior."
            : "Your catalog has logged " + formatHours(totalLearningMinutes) + " of tracked study time so far.";

        return new DashboardDtos.AdminDashboardResponse(
            toSessionUser(admin),
            headline,
            stats,
            buildActivityPoints(weeklySessions),
            performance,
            learnerSpotlights
        );
    }

    public AuthDtos.SessionUser toSessionUser(User user) {
        return new AuthDtos.SessionUser(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    public void autoEnrollStarterCourses(User user) {
        courseRepository.findAll().stream()
            .limit(2)
            .filter(course -> !enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId()))
            .map(course -> new Enrollment(user, course, LocalDate.now()))
            .forEach(enrollmentRepository::save);
    }

    private DashboardDtos.CoursePerformance buildCoursePerformance(Course course) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
        int enrollmentCount = enrollments.size();
        int moduleCount = orderedModules(course).size();

        List<Integer> progressValues = enrollments.stream()
            .map(enrollment -> calculateCourseProgress(
                course,
                enrollment.getUser().getId(),
                getProgressMap(enrollment.getUser().getId(), course.getId())
            ).progressPercent())
            .toList();

        int averageProgress = progressValues.isEmpty()
            ? 0
            : (int) Math.round(progressValues.stream().mapToInt(Integer::intValue).average().orElse(0));

        List<ProgressRecord> records = progressRecordRepository.findByModuleCourseId(course.getId());
        long totalMinutes = records.stream()
            .map(ProgressRecord::getTimeSpentMinutes)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .sum();

        String bottleneckModule = orderedModules(course).stream()
            .max(Comparator.comparingLong(module -> records.stream()
                .filter(record -> record.getModule().getId().equals(module.getId()))
                .map(ProgressRecord::getTimeSpentMinutes)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum()))
            .map(CourseModule::getTitle)
            .orElse("No module data yet");

        return new DashboardDtos.CoursePerformance(
            course.getId(),
            course.getTitle(),
            course.getCategory(),
            enrollmentCount,
            moduleCount,
            averageProgress,
            bottleneckModule,
            totalMinutes
        );
    }

    private DashboardDtos.LearnerSpotlight buildLearnerSpotlight(User user) {
        List<ProgressRecord> progressRecords = progressRecordRepository.findByUserId(user.getId());
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(user.getId());

        long minutesSpent = progressRecords.stream()
            .map(ProgressRecord::getTimeSpentMinutes)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .sum();

        int completedModules = (int) progressRecords.stream().filter(ProgressRecord::isCompleted).count();
        int activeCourses = (int) enrollments.stream()
            .filter(enrollment -> calculateCourseProgress(
                enrollment.getCourse(),
                user.getId(),
                getProgressMap(user.getId(), enrollment.getCourse().getId())
            ).progressPercent() < 100)
            .count();

        return new DashboardDtos.LearnerSpotlight(
            user.getFullName(),
            user.getEmail(),
            minutesSpent,
            completedModules,
            activeCourses
        );
    }

    private CourseDtos.CourseSummary buildCourseSummary(Course course, Long userId, Map<Long, ProgressRecord> progressMap) {
        CourseProgressStats stats = calculateCourseProgress(course, userId, progressMap);
        return new CourseDtos.CourseSummary(
            course.getId(),
            course.getTitle(),
            course.getCategory(),
            course.getLevel(),
            course.getInstructor(),
            course.getDurationHours(),
            course.getDescription(),
            course.getHeroAccent(),
            stats.progressPercent(),
            stats.completedModules(),
            stats.totalModules(),
            stats.completedVideos(),
            stats.totalVideos(),
            stats.totalMinutesSpent(),
            stats.nextModule()
        );
    }

    private CourseDtos.ModuleProgress buildModuleProgress(User user, CourseModule module, ProgressRecord record) {
        List<UserProgress> videoProgressList = user == null ? List.of() : userProgressRepository.findByUserIdAndModuleId(user.getId(), module.getId());
        Map<Long, Boolean> completionMap = videoProgressList.stream()
            .collect(Collectors.toMap(vp -> vp.getVideo().getId(), UserProgress::isCompleted));

        List<CourseDtos.VideoDetail> videos = module.getVideos().stream()
            .map(v -> new CourseDtos.VideoDetail(
                v.getId(),
                v.getTitle(),
                v.getVideoUrl(),
                v.getSequenceOrder(),
                completionMap.getOrDefault(v.getId(), false)
            ))
            .toList();

        return new CourseDtos.ModuleProgress(
            module.getId(),
            module.getSequenceOrder(),
            module.getTitle(),
            module.getSummary(),
            module.getContent(),
            module.getEstimatedMinutes(),
            record != null && record.isCompleted(),
            record == null || record.getTimeSpentMinutes() == null ? 0L : record.getTimeSpentMinutes(),
            record == null ? null : record.getLastAccessed(),
            record == null ? null : record.getCompletedAt(),
            videos
        );
    }

    private CourseProgressStats calculateCourseProgress(Course course, Long userId, Map<Long, ProgressRecord> progressMap) {
        List<CourseModule> modules = courseModuleRepository.findByCourseIdOrderBySequenceOrderAsc(course.getId());
        int totalModules = modules.size();
        int completedModules = (int) modules.stream()
            .map(module -> progressMap.get(module.getId()))
            .filter(Objects::nonNull)
            .filter(ProgressRecord::isCompleted)
            .count();

        // New Video-based progress logic - counting across ALL modules
        int totalVideos = (int) moduleVideoRepository.countByModuleCourseId(course.getId());
        int completedVideos = 0;
        
        if (userId != null) {
            completedVideos = (int) userProgressRepository.countByUserIdAndCourseIdAndCompleted(userId, course.getId(), true);
        }

        // Progress Calculation - Fallback to modules if no videos exist
        int progressPercent;
        if (totalVideos > 0) {
            progressPercent = (int) Math.round((completedVideos * 100.0) / totalVideos);
        } else if (totalModules > 0) {
            // If no videos, progress is based on module completion
            progressPercent = (int) Math.round((completedModules * 100.0) / totalModules);
        } else {
            progressPercent = 0;
        }

        // DEBUG LOGGING - Requirement 9
        System.out.println("[PROGRESS DEBUG] Course: " + course.getTitle());
        System.out.println("- Total Videos (calculated): " + totalVideos);
        System.out.println("- Completed Videos (found): " + completedVideos);
        System.out.println("- Total Modules: " + totalModules);
        System.out.println("- Completed Modules: " + completedModules);
        System.out.println("- Final Progress: " + progressPercent + "%");

        long totalMinutesSpent = modules.stream()
            .map(module -> progressMap.get(module.getId()))
            .filter(Objects::nonNull)
            .map(ProgressRecord::getTimeSpentMinutes)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .sum();

        String toughestModule = modules.stream()
            .max(Comparator.comparingLong(module -> Optional.ofNullable(progressMap.get(module.getId()))
                .map(ProgressRecord::getTimeSpentMinutes)
                .orElse(0L)))
            .map(CourseModule::getTitle)
            .orElse("No module data yet");

        String nextModule = modules.stream()
            .filter(module -> {
                ProgressRecord record = progressMap.get(module.getId());
                return record == null || !record.isCompleted();
            })
            .map(CourseModule::getTitle)
            .findFirst()
            .orElse("Course completed");

        return new CourseProgressStats(progressPercent, completedModules, totalModules, completedVideos, totalVideos, totalMinutesSpent, toughestModule, nextModule);
    }

    private List<CourseModule> orderedModules(Course course) {
        return course.getModules().stream()
            .sorted(Comparator.comparing(CourseModule::getSequenceOrder))
            .toList();
    }

    private Map<Long, ProgressRecord> getProgressMap(Long userId, Long courseId) {
        return progressRecordRepository.findByUserIdAndModuleCourseId(userId, courseId).stream()
            .collect(Collectors.toMap(record -> record.getModule().getId(), record -> record));
    }

    private List<DashboardDtos.ActivityPoint> buildActivityPoints(List<LearningSession> sessions) {
        LocalDate startDate = LocalDate.now().minusDays(6);
        Map<LocalDate, Long> totals = new LinkedHashMap<>();
        for (int index = 0; index < 7; index++) {
            totals.put(startDate.plusDays(index), 0L);
        }

        for (LearningSession session : sessions) {
            totals.computeIfPresent(
                session.getSessionDate(),
                (date, total) -> total + Objects.requireNonNullElse(session.getMinutesSpent(), 0L)
            );
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE");
        return totals.entrySet().stream()
            .map(entry -> new DashboardDtos.ActivityPoint(entry.getKey().format(formatter), entry.getValue()))
            .toList();
    }

    private String formatHours(long totalMinutes) {
        return String.format("%.1fh", totalMinutes / 60.0);
    }

    private record CourseProgressStats(
        int progressPercent,
        int completedModules,
        int totalModules,
        int completedVideos,
        int totalVideos,
        long totalMinutesSpent,
        String toughestModule,
        String nextModule
    ) {}
}
