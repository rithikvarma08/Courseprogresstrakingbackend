package com.learnflow.backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_activity")
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private ModuleVideo video;

    @Column(nullable = false)
    private Integer minutesSpent;

    @Column(nullable = false)
    private LocalDate activityDate;

    public UserActivity() {}

    public UserActivity(User user, ModuleVideo video, Integer minutesSpent, LocalDate activityDate) {
        this.user = user;
        this.video = video;
        this.minutesSpent = minutesSpent;
        this.activityDate = activityDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ModuleVideo getVideo() { return video; }
    public void setVideo(ModuleVideo video) { this.video = video; }

    public Integer getMinutesSpent() { return minutesSpent; }
    public void setMinutesSpent(Integer minutesSpent) { this.minutesSpent = minutesSpent; }

    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
}
