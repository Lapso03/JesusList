package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;

public record GameListDto(
        Long id,
        String slug,
        String name,
        String emoji,
        String description,
        boolean hasDifficulty
) {
    public static GameListDto from(GameList gameList) {
        return new GameListDto(
                gameList.getId(), gameList.getSlug(), gameList.getName(), gameList.getEmoji(),
                gameList.getDescription(), gameList.isHasDifficulty()
        );
    }
}