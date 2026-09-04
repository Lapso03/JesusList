package com.lapso.gdtracker.service;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.repository.GameListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

@Service
public class GameListAdminService {

    private final GameListRepository gameListRepository;
    private final LevelAdminService levelAdminService;

    public GameListAdminService(GameListRepository gameListRepository, LevelAdminService levelAdminService) {
        this.gameListRepository = gameListRepository;
        this.levelAdminService = levelAdminService;
    }

    @Transactional
    public GameList create(String name, String emoji, String description, boolean hasDifficulty) {
        String slug = uniqueSlugFor(name);
        int nextOrder = gameListRepository.findAllByOrderByDisplayOrderAsc().size();
        GameList gameList = new GameList(slug, name, emoji, description, hasDifficulty, nextOrder);
        return gameListRepository.save(gameList);
    }

    @Transactional
    public GameList update(GameList gameList, String name, String emoji, String description, boolean hasDifficulty) {
        gameList.setName(name);
        gameList.setEmoji(emoji);
        gameList.setDescription(description);
        gameList.setHasDifficulty(hasDifficulty);
        return gameListRepository.save(gameList);
    }

    /** Borra la lista Y todos sus niveles (con su progreso y comentarios). Irreversible. */
    @Transactional
    public void delete(GameList gameList) {
        levelAdminService.deleteAllLevelsOf(gameList);
        gameListRepository.delete(gameList);
    }

    public List<GameList> findAll() {
        return gameListRepository.findAllByOrderByDisplayOrderAsc();
    }

    private String uniqueSlugFor(String name) {
        String base = slugify(name);
        if (base.isBlank()) base = "lista";
        String slug = base;
        int suffix = 2;
        while (gameListRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix;
            suffix++;
        }
        return slug;
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return normalized.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }
}