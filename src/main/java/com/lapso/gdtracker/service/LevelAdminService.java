package com.lapso.gdtracker.service;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.Progress;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.LevelCommentRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.repository.ProgressRepository;
import com.lapso.gdtracker.service.sync.AredlClient;
import com.lapso.gdtracker.service.sync.GddlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LevelAdminService {

    private final LevelRepository levelRepository;
    private final AppUserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final LevelCommentRepository levelCommentRepository;
    private final AredlClient aredlClient;
    private final GddlClient gddlClient;

    public LevelAdminService(LevelRepository levelRepository, AppUserRepository userRepository,
                             ProgressRepository progressRepository, LevelCommentRepository levelCommentRepository,
                             AredlClient aredlClient, GddlClient gddlClient) {
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.levelCommentRepository = levelCommentRepository;
        this.aredlClient = aredlClient;
        this.gddlClient = gddlClient;
    }

    @Transactional
    public Level createLevel(GameList gameList, int position, String name, Long gdId, String staticDifficulty, String showcaseVideoUrl) {
        List<Level> toShift = levelRepository
                .findByGameListAndPositionGreaterThanEqualOrderByPositionDesc(gameList, position);
        for (Level l : toShift) {
            l.setPosition(l.getPosition() + 1);
            levelRepository.save(l);
        }
        levelRepository.flush();

        Level level = new Level(gameList, position, name, gdId, staticDifficulty, showcaseVideoUrl);
        level = levelRepository.save(level);

        for (AppUser user : userRepository.findAll()) {
            progressRepository.save(new Progress(level, user, 0));
        }

        // Antes esto solo pasaba para Classic; ahora se sincroniza cualquier nivel con ID de GD,
        // sea de la lista que sea (siempre que la lista use dificultad/sync).
        if (gdId != null && gameList.isHasDifficulty()) {
            syncOne(level);
        }

        return level;
    }

    @Transactional
    public void updateDetails(Level level, String name, Long gdId, String staticDifficulty, String showcaseVideoUrl) {
        level.setName(name);
        level.setGdId(gdId);
        level.setStaticDifficulty(staticDifficulty);
        level.setShowcaseVideoUrl(showcaseVideoUrl);
        levelRepository.save(level);
    }

    @Transactional
    public void moveTo(Level level, int newPosition) {
        int oldPosition = level.getPosition();
        if (newPosition == oldPosition) return;

        GameList gameList = level.getGameList();

        if (newPosition < oldPosition) {
            List<Level> range = levelRepository
                    .findByGameListAndPositionBetweenOrderByPositionDesc(gameList, newPosition, oldPosition - 1);
            for (Level l : range) {
                l.setPosition(l.getPosition() + 1);
                levelRepository.save(l);
            }
        } else {
            List<Level> range = levelRepository
                    .findByGameListAndPositionBetweenOrderByPositionAsc(gameList, oldPosition + 1, newPosition);
            for (Level l : range) {
                l.setPosition(l.getPosition() - 1);
                levelRepository.save(l);
            }
        }
        levelRepository.flush();

        level.setPosition(newPosition);
        levelRepository.save(level);
    }

    /**
     * Borra un nivel. Antes fallaba en Postgres porque Progress y LevelComment tienen una foreign
     * key hacia levels sin cascada: hay que borrar primero esas filas hijas.
     */
    @Transactional
    public void deleteLevel(Level level) {
        GameList gameList = level.getGameList();
        int position = level.getPosition();

        levelCommentRepository.deleteAll(levelCommentRepository.findByLevelOrderByCreatedAtAsc(level));
        progressRepository.deleteAll(progressRepository.findByLevel(level));
        levelRepository.delete(level);
        levelRepository.flush();

        List<Level> toShift = levelRepository
                .findByGameListAndPositionGreaterThanOrderByPositionAsc(gameList, position);
        for (Level l : toShift) {
            l.setPosition(l.getPosition() - 1);
            levelRepository.save(l);
        }
    }

    /** Usado al borrar una lista entera: borra todos sus niveles (y su progreso/comentarios) uno a uno. */
    @Transactional
    public void deleteAllLevelsOf(GameList gameList) {
        List<Level> levels = levelRepository.findByGameListOrderByPositionAsc(gameList);
        for (int i = levels.size() - 1; i >= 0; i--) {
            deleteLevel(levels.get(i));
        }
    }

    public int nextPosition(GameList gameList) {
        return levelRepository.countByGameList(gameList) + 1;
    }

    @Transactional
    public void syncOne(Level level) {
        if (level.getGdId() == null || !level.getGameList().isHasDifficulty()) return;

        Integer aredlPos = aredlClient.fetchPosition(level.getGdId());
        String gddlDiff = gddlClient.fetchDifficulty(level.getGdId());

        if (aredlPos != null) level.setAredlPosition(aredlPos);
        if (gddlDiff != null) level.setGddlDifficulty(gddlDiff);
        level.setLastSyncedAt(LocalDateTime.now());

        levelRepository.save(level);
    }
}