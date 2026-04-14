package com.learnflow.backend.controllers;

import com.learnflow.backend.models.ModuleVideo;
import com.learnflow.backend.models.User;
import com.learnflow.backend.models.UserActivity;
import com.learnflow.backend.repositories.ModuleVideoRepository;
import com.learnflow.backend.repositories.UserActivityRepository;
import com.learnflow.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private ModuleVideoRepository moduleVideoRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/update-time")
    public ResponseEntity<?> updateStudyTime(@RequestBody Map<String, Object> payload, Authentication authentication) {
        User userDetails = (User) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        Long videoId = Long.valueOf(payload.get("videoId").toString());
        Integer minutes = Integer.valueOf(payload.get("minutes").toString());
        LocalDate today = LocalDate.now();

        Optional<UserActivity> existing = userActivityRepository.findByUserIdAndVideoIdAndActivityDate(userId, videoId, today);

        UserActivity activity;
        if (existing.isPresent()) {
            activity = existing.get();
            activity.setMinutesSpent(activity.getMinutesSpent() + minutes);
        } else {
            User user = userRepository.findById(userId).orElseThrow();
            ModuleVideo video = moduleVideoRepository.findById(videoId).orElseThrow();
            activity = new UserActivity(user, video, minutes, today);
        }

        userActivityRepository.save(activity);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("currentTotal", activity.getMinutesSpent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/study-time/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getStudyTime(@PathVariable Long userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<UserActivity> activities = userActivityRepository.findByUserIdAndActivityDateBetween(userId, start, end);
        
        Map<LocalDate, Integer> dailyMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            dailyMap.put(start.plusDays(i), 0);
        }

        for (UserActivity activity : activities) {
            dailyMap.put(activity.getActivityDate(), dailyMap.get(activity.getActivityDate()) + activity.getMinutesSpent());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE");
        
        for (int i = 0; i < 7; i++) {
            LocalDate dateUnit = start.plusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("label", dateUnit.format(formatter));
            point.put("minutes", dailyMap.get(dateUnit));
            point.put("date", dateUnit.toString());
            result.add(point);
        }

        return ResponseEntity.ok(result);
    }
}
