package com.learnflow.backend.repositories;

import com.learnflow.backend.models.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findByCourseIdOrderBySequenceOrderAsc(Long courseId);
    long countByCourseId(Long courseId);
}
