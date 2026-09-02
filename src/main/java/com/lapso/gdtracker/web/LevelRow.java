package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.Level;

import java.util.Map;

public class LevelRow {
    private final Level level;
    private final Map<String, Integer> progressByUser;

    public LevelRow(Level level, Map<String, Integer> progressByUser) {
        this.level = level;
        this.progressByUser = progressByUser;
    }

    public Level getLevel() {
        return level;
    }

    public Map<String, Integer> getProgressByUser() {
        return progressByUser;
    }

    public double getPoints() {
        return level.points();
    }
}
