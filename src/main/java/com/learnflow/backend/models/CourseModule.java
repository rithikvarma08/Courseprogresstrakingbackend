package com.learnflow.backend.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "modules")
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer sequenceOrder;

    private Integer estimatedMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private java.util.List<ModuleVideo> videos = new java.util.ArrayList<>();

    public CourseModule() {}

    public CourseModule(String title, String summary, String content, Integer sequenceOrder, Integer estimatedMinutes, Course course) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.sequenceOrder = sequenceOrder;
        this.estimatedMinutes = estimatedMinutes;
        this.course = course;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public java.util.List<ModuleVideo> getVideos() { return videos; }
    public void setVideos(java.util.List<ModuleVideo> videos) { this.videos = videos; }
}
