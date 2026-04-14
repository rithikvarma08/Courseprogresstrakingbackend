package com.learnflow.backend.repositories;

import com.learnflow.backend.models.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUserIdAndVideoId(Long userId, Long videoId);
    List<UserProgress> findByUserIdAndModuleId(Long userId, Long moduleId);
    List<UserProgress> findByUserIdAndCourseId(Long userId, Long courseId);
    long countByUserIdAndCourseIdAndCompleted(Long userId, Long courseId, boolean completed);
}


