package com.lapso.gdtracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "progress", uniqueConstraints = @UniqueConstraint(columnNames = {"level_id", "user_id"}))
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "level_id")
    private Level level;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    /** 0-100. En Platformer solo se usan 0 o 100 (no completado / completado). */
    @Column(nullable = false)
    private int percentage;

    protected Progress() {
    }

    public Progress(Level level, AppUser user, int percentage) {
        this.level = level;
        this.user = user;
        this.percentage = percentage;
    }

    public boolean isCompleted() {
        return percentage >= 100;
    }

    public Long getId() {
        return id;
    }

    public Level getLevel() {
        return level;
    }

    public AppUser getUser() {
        return user;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = Math.max(0, Math.min(100, percentage));
    }
}
