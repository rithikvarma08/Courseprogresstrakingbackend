package com.learnflow.backend.repositories;

import com.learnflow.backend.models.ProgressRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProgressRecordRepository extends JpaRepository<ProgressRecord, Long> {
    List<ProgressRecord> findByUserId(Long userId);
    List<ProgressRecord> findByUserIdAndModuleCourseId(Long userId, Long courseId);
    List<ProgressRecord> findByModuleCourseId(Long courseId);
    Optional<ProgressRecord> findByUserIdAndModuleId(Long userId, Long moduleId);
    long countByCompletedTrue();
}
