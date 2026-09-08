package com.lapso.gdtracker.service;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.Progress;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.repository.ProgressRepository;
import com.lapso.gdtracker.service.sync.GdBrowserClient;
import com.lapso.gdtracker.service.sync.GdLevelSuggestion;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 5;

    private final LevelRepository levelRepository;
    private final ProgressRepository progressRepository;
    private final GdBrowserClient gdBrowserClient;

    public RecommendationService(LevelRepository levelRepository, ProgressRepository progressRepository,
                                 GdBrowserClient gdBrowserClient) {
        this.levelRepository = levelRepository;
        this.progressRepository = progressRepository;
        this.gdBrowserClient = gdBrowserClient;
    }

    public List<Level> recommendFor(AppUser user, GameList gameList) {
        List<Level> levels = levelRepository.findByGameListOrderByPositionAsc(gameList);

        Set<Long> levelIds = levels.stream().map(Level::getId).collect(Collectors.toSet());
        List<Progress> progress = progressRepository.findByUser(user).stream()
                .filter(p -> levelIds.contains(p.getLevel().getId()))
                .toList();

        Set<Long> completedIds = progress.stream()
                .filter(Progress::isCompleted)
                .map(p -> p.getLevel().getId())
                .collect(Collectors.toSet());

        List<Level> completedLevels = levels.stream()
                .filter(l -> completedIds.contains(l.getId()))
                .toList();

        List<Level> pending = levels.stream()
                .filter(l -> !completedIds.contains(l.getId()))
                .toList();

        if (completedLevels.isEmpty()) {
            return pending.stream()
                    .sorted(Comparator.comparingInt(Level::getPosition).reversed())
                    .limit(MAX_RECOMMENDATIONS)
                    .toList();
        }

        double avgPosition = completedLevels.stream()
                .mapToInt(Level::getPosition)
                .average()
                .orElse(0);

        return pending.stream()
                .sorted(Comparator.comparingDouble(l -> Math.abs(l.getPosition() - avgPosition)))
                .limit(MAX_RECOMMENDATIONS)
                .toList();
    }

    /**
     * Antes estaba fijado a Classic; ahora funciona para cualquier lista, filtrando los resultados
     * de GDBrowser para que coincidan con el modo de la lista actual: si la lista usa dificultad
     * (como Classic) solo se recomiendan niveles en modo Classic; si no (como Platformer) solo se
     * recomiendan niveles en modo Platformer.
     */
    public List<GdLevelSuggestion> recommendExternalDemons(AppUser user, GameList gameList) {
        List<Level> levels = levelRepository.findByGameListOrderByPositionAsc(gameList);

        Set<Long> levelIds = levels.stream().map(Level::getId).collect(Collectors.toSet());
        List<Progress> progress = progressRepository.findByUser(user).stream()
                .filter(p -> levelIds.contains(p.getLevel().getId()))
                .toList();

        Set<Long> completedIds = progress.stream()
                .filter(Progress::isCompleted)
                .map(p -> p.getLevel().getId())
                .collect(Collectors.toSet());

        List<Level> completedLevels = levels.stream()
                .filter(l -> completedIds.contains(l.getId()))
                .toList();

        if (completedLevels.isEmpty()) {
            return List.of();
        }

        int demonFilter;
        if (gameList.isHasDifficulty()) {
            double avgPosition = completedLevels.stream().mapToInt(Level::getPosition).average().orElse(0);
            Level closest = completedLevels.stream()
                    .min(Comparator.comparingDouble(l -> Math.abs(l.getPosition() - avgPosition)))
                    .orElseThrow();
            demonFilter = demonFilterFor(closest.getGddlDifficulty() != null ? closest.getGddlDifficulty() : closest.getStaticDifficulty());
        } else {
            // Las listas sin dificultad (como Platformer) no tienen forma de estimar el nivel del
            // usuario, asi que recomendamos del rango medio-alto (Insane) para no quedarnos cortos.
            demonFilter = 4;
        }

        boolean wantPlatformer = !gameList.isHasDifficulty();

        Set<Long> knownGdIds = levels.stream()
                .map(Level::getGdId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        return gdBrowserClient.searchDemons(demonFilter, wantPlatformer, 10).stream()
                .filter(s -> !knownGdIds.contains(s.id()))
                .limit(MAX_RECOMMENDATIONS)
                .toList();
    }

    private int demonFilterFor(String difficultyText) {
        if (difficultyText == null) return 5;
        String d = difficultyText.toLowerCase();
        if (d.contains("easy")) return 1;
        if (d.contains("medium")) return 2;
        if (d.contains("hard")) return 3;
        if (d.contains("insane")) return 4;
        return 5;
    }
}