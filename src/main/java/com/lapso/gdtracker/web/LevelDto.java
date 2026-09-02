package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.Level;

public record LevelDto(
        Long id,
        int position,
        String name,
        Long gdId,
        String staticDifficulty,
        Integer aredlPosition,
        String gddlDifficulty,
        String showcaseVideoUrl
) {
    public static LevelDto from(Level level) {
        return new LevelDto(
                level.getId(), level.getPosition(), level.getName(), level.getGdId(),
                level.getStaticDifficulty(), level.getAredlPosition(), level.getGddlDifficulty(),
                level.getShowcaseVideoUrl()
        );
    }
}