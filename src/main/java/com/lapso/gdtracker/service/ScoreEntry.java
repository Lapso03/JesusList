package com.lapso.gdtracker.service;

public class ScoreEntry {
    private final String username;
    private final double points;
    private final long levelsCompleted;

    public ScoreEntry(String username, double points, long levelsCompleted) {
        this.username = username;
        this.points = points;
        this.levelsCompleted = levelsCompleted;
    }

    public String getUsername() {
        return username;
    }

    public double getPoints() {
        return points;
    }

    public long getLevelsCompleted() {
        return levelsCompleted;
    }
}
