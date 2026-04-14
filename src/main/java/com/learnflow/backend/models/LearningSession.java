package com.learnflow.backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "learning_sessions")
public class LearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private Long minutesSpent;

    public LearningSession() {}

    public LearningSession(User user, CourseModule module, LocalDate sessionDate, Long minutesSpent) {
        this.user = user;
        this.module = module;
        this.sessionDate = sessionDate;
        this.minutesSpent = minutesSpent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CourseModule getModule() { return module; }
    public void setModule(CourseModule module) { this.module = module; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public Long getMinutesSpent() { return minutesSpent; }
    public void setMinutesSpent(Long minutesSpent) { this.minutesSpent = minutesSpent; }
}
