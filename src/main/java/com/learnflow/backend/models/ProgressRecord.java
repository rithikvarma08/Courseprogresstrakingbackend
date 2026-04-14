package com.learnflow.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "progress_records",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"})
)
public class ProgressRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    private boolean completed;

    private Long timeSpentMinutes;

    private LocalDateTime lastAccessed;

    private LocalDateTime completedAt;

    public ProgressRecord() {}

    public ProgressRecord(User user, CourseModule module, boolean completed, Long timeSpentMinutes, LocalDateTime lastAccessed) {
        this.user = user;
        this.module = module;
        this.completed = completed;
        this.timeSpentMinutes = timeSpentMinutes;
        this.lastAccessed = lastAccessed;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CourseModule getModule() { return module; }
    public void setModule(CourseModule module) { this.module = module; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Long getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(Long timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
