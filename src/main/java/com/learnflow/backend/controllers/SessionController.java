package com.learnflow.backend.controllers;

import com.learnflow.backend.models.CourseModule;
import com.learnflow.backend.models.LearningSession;
import com.learnflow.backend.models.User;
import com.learnflow.backend.repositories.CourseModuleRepository;
import com.learnflow.backend.repositories.LearningSessionRepository;
import com.learnflow.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private LearningSessionRepository sessionRepository;

    @Autowired
    private CourseModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{moduleId}")
    public LearningSession recordSession(@PathVariable Long moduleId, @RequestBody Map<String, Long> payload, Authentication authentication) {
        User userDetails = (User) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        Long minutes = payload.getOrDefault("minutes", 0L);
        LocalDate today = LocalDate.now();

        // Check if we already have a session for this user/module today
        Optional<LearningSession> existing = sessionRepository.findByUserId(user.getId()).stream()
                .filter(s -> s.getModule().getId().equals(moduleId) && s.getSessionDate().equals(today))
                .findFirst();

        LearningSession session;
        if (existing.isPresent()) {
            session = existing.get();
            session.setMinutesSpent(session.getMinutesSpent() + minutes);
        } else {
            session = new LearningSession(user, module, today, minutes);
        }

        return sessionRepository.save(session);
    }

    @GetMapping("/weekly")
    public Map<String, Long> getWeeklyStats(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        
        List<LearningSession> sessions = sessionRepository.findByUserId(user.getId());
        
        Map<String, Long> dailyMinutes = new HashMap<>();
        // Initialize last 7 days with 0
        for (int i = 0; i < 7; i++) {
            dailyMinutes.put(LocalDate.now().minusDays(i).toString(), 0L);
        }

        for (LearningSession session : sessions) {
            if (session.getSessionDate().isAfter(sevenDaysAgo)) {
                String date = session.getSessionDate().toString();
                dailyMinutes.put(date, dailyMinutes.getOrDefault(date, 0L) + session.getMinutesSpent());
            }
        }

        return dailyMinutes;
    }
}
