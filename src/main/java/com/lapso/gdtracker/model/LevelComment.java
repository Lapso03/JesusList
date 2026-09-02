package com.lapso.gdtracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "level_comments")
public class LevelComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "level_id")
    private Level level;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected LevelComment() {
    }

    public LevelComment(Level level, AppUser user, String text) {
        this.level = level;
        this.user = user;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Level getLevel() { return level; }
    public AppUser getUser() { return user; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}