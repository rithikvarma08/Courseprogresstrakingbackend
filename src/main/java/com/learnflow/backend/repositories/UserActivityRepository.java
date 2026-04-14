package com.learnflow.backend.repositories;

import com.learnflow.backend.models.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserIdAndActivityDateBetween(Long userId, LocalDate start, LocalDate end);
    Optional<UserActivity> findByUserIdAndVideoIdAndActivityDate(Long userId, Long videoId, LocalDate date);
}
