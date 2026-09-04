package com.lapso.gdtracker.web;

public record GameListRequest(
        String name,
        String emoji,
        String description,
        boolean hasDifficulty
) {
}