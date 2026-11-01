package com.lapso.gdtracker.service;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.LevelLogEntry;
import com.lapso.gdtracker.model.LevelLogType;
import com.lapso.gdtracker.model.Progress;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.LevelCommentRepository;
import com.lapso.gdtracker.repository.LevelLogRepository;
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
    private final LevelLogRepository levelLogRepository;
    private final AredlClient aredlClient;
    private final GddlClient gddlClient;

    public LevelAdminService(LevelRepository levelRepository, AppUserRepository userRepository,
                             ProgressRepository progressRepository, LevelCommentRepository levelCommentRepository,
                             LevelLogRepository levelLogRepository,
                             AredlClient aredlClient, GddlClient gddlClient) {
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.levelCommentRepository = levelCommentRepository;
        this.levelLogRepository = levelLogRepository;
        this.aredlClient = aredlClient;
        this.gddlClient = gddlClient;
    }

    /**
     * Añade un nivel en la posicion indicada (se ajusta al rango valido: no se pueden dejar huecos).
     * Si entra dentro de la Main List y esta ya tiene 100 niveles, el que estaba en el 100 pasa a
     * la Legacy List automaticamente, y ambos movimientos quedan en el historial.
     *
     * @param actor usuario que añade el nivel (queda registrado en el historial)
     */
    @Transactional
    public Level createLevel(GameList gameList, int requestedPosition, String name, Long gdId,
                             String staticDifficulty, String showcaseVideoUrl, String actor) {
        int total = levelRepository.countByGameList(gameList);
        int position = Math.max(1, Math.min(requestedPosition, total + 1));

        List<Level> toShift = levelRepository
                .findByGameListAndPositionGreaterThanEqualOrderByPositionDesc(gameList, position);
        for (Level l : toShift) {
            int from = l.getPosition();
            l.setPosition(from + 1);
            levelRepository.save(l);
            logBoundaryCrossing(gameList, l, from, from + 1, actor);
        }
        levelRepository.flush();

        Level level = new Level(gameList, position, name, gdId, staticDifficulty, showcaseVideoUrl);
        level = levelRepository.save(level);

        for (AppUser user : userRepository.findAll()) {
            progressRepository.save(new Progress(level, user, 0));
        }

        log(gameList, LevelLogType.ADDED, name, null, position, actor);

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

    /** Mueve un nivel a otra posicion de su lista. Los cruces Main/Legacy de otros niveles se registran. */
    @Transactional
    public void moveTo(Level level, int requestedPosition, String actor) {
        GameList gameList = level.getGameList();
        int total = levelRepository.countByGameList(gameList);
        int newPosition = Math.max(1, Math.min(requestedPosition, total));
        int oldPosition = level.getPosition();
        if (newPosition == oldPosition) return;

        // Aparcamos el nivel en una posicion temporal (0) para que, al desplazar a los demas, ninguno
        // choque un instante con su posicion actual (restriccion unica lista + posicion).
        level.setPosition(0);
        levelRepository.saveAndFlush(level);

        if (newPosition < oldPosition) {
            List<Level> range = levelRepository
                    .findByGameListAndPositionBetweenOrderByPositionDesc(gameList, newPosition, oldPosition - 1);
            for (Level l : range) {
                int from = l.getPosition();
                l.setPosition(from + 1);
                levelRepository.save(l);
                logBoundaryCrossing(gameList, l, from, from + 1, actor);
            }
        } else {
            List<Level> range = levelRepository
                    .findByGameListAndPositionBetweenOrderByPositionAsc(gameList, oldPosition + 1, newPosition);
            for (Level l : range) {
                int from = l.getPosition();
                l.setPosition(from - 1);
                levelRepository.save(l);
                logBoundaryCrossing(gameList, l, from, from - 1, actor);
            }
        }
        levelRepository.flush();

        level.setPosition(newPosition);
        levelRepository.save(level);

        log(gameList, LevelLogType.MOVED, level.getName(), oldPosition, newPosition, actor);
    }

    /**
     * Borra un nivel. Antes fallaba en Postgres porque Progress y LevelComment tienen una foreign
     * key hacia levels sin cascada: hay que borrar primero esas filas hijas.
     * Si estaba en la Main List y hay Legacy, el primero de la Legacy sube a la Main (queda registrado).
     */
    @Transactional
    public void deleteLevel(Level level, String actor) {
        GameList gameList = level.getGameList();
        int position = level.getPosition();
        String name = level.getName();

        removeWithDependents(level);

        List<Level> toShift = levelRepository
                .findByGameListAndPositionGreaterThanOrderByPositionAsc(gameList, position);
        for (Level l : toShift) {
            int from = l.getPosition();
            l.setPosition(from - 1);
            levelRepository.save(l);
            logBoundaryCrossing(gameList, l, from, from - 1, actor);
        }

        log(gameList, LevelLogType.REMOVED, name, position, null, actor);
    }

    /**
     * Usado al borrar una lista entera: borra todos sus niveles (y su progreso/comentarios).
     * No registra nada en el historial porque la lista (y su historial) desaparecen igualmente.
     */
    @Transactional
    public void deleteAllLevelsOf(GameList gameList) {
        for (Level level : levelRepository.findByGameListOrderByPositionAsc(gameList)) {
            removeWithDependents(level);
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

    private void removeWithDependents(Level level) {
        levelCommentRepository.deleteAll(levelCommentRepository.findByLevelOrderByCreatedAtAsc(level));
        progressRepository.deleteAll(progressRepository.findByLevel(level));
        levelRepository.delete(level);
        levelRepository.flush();
    }

    /**
     * Cuando un nivel se desplaza solo (porque otro entra, sale o se mueve), solo nos interesa dejar
     * constancia si cruza la frontera Main/Legacy. El resto de desplazamientos de +1/-1 no se registran.
     */
    private void logBoundaryCrossing(GameList gameList, Level level, int oldPos, int newPos, String actor) {
        boolean wasMain = oldPos <= Level.MAIN_LIST_SIZE;
        boolean isMain = newPos <= Level.MAIN_LIST_SIZE;
        if (wasMain && !isMain) {
            log(gameList, LevelLogType.MOVED_TO_LEGACY, level.getName(), oldPos, newPos, actor);
        } else if (!wasMain && isMain) {
            log(gameList, LevelLogType.MOVED_TO_MAIN, level.getName(), oldPos, newPos, actor);
        }
    }

    private void log(GameList gameList, LevelLogType type, String levelName,
                     Integer oldPosition, Integer newPosition, String actor) {
        levelLogRepository.save(new LevelLogEntry(gameList, type, levelName, oldPosition, newPosition, actor));
    }
}
