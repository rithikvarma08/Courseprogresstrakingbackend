package com.learnflow.backend.repositories;

import com.learnflow.backend.models.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
    List<LearningSession> findByUserId(Long userId);
    List<LearningSession> findByUserIdAndSessionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<LearningSession> findBySessionDateBetween(LocalDate startDate, LocalDate endDate);
}
