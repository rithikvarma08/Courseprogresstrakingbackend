package com.learnflow.backend.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "module_videos")
public class ModuleVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String videoUrl; // YouTube embed URL or similar

    @Column(nullable = false)
    private Integer sequenceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnore
    private CourseModule module;

    public ModuleVideo() {}

    public ModuleVideo(String title, String videoUrl, Integer sequenceOrder, CourseModule module) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.sequenceOrder = sequenceOrder;
        this.module = module;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getVideoUrl() { return videoUrl; }
    public Integer getSequenceOrder() { return sequenceOrder; }
    public CourseModule getModule() { return module; }

    public void setTitle(String title) { this.title = title; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public void setModule(CourseModule module) { this.module = module; }
}
