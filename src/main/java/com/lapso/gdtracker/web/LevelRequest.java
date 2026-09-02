package com.lapso.gdtracker.web;

public record LevelRequest(
        String tipo,
        int position,
        String name,
        Long gdId,
        String staticDifficulty,
        String showcaseVideoUrl
) {
}