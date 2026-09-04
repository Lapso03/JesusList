package com.lapso.gdtracker.service.sync;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.repository.GameListRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sincroniza la posicion AREDL y la dificultad GDDL de cada nivel con ID de GD, en cualquier
 * lista que tenga hasDifficulty=true (antes esto estaba fijado solo a Classic). Se ejecuta una
 * vez al arrancar la app y luego cada 24h via cron.
 */
@Service
public class LevelSyncService {

    private final LevelRepository levelRepository;
    private final GameListRepository gameListRepository;
    private final AredlClient aredlClient;
    private final GddlClient gddlClient;

    public LevelSyncService(LevelRepository levelRepository, GameListRepository gameListRepository,
                            AredlClient aredlClient, GddlClient gddlClient) {
        this.levelRepository = levelRepository;
        this.gameListRepository = gameListRepository;
        this.aredlClient = aredlClient;
        this.gddlClient = gddlClient;
    }

    /**
     * @Scheduled con cron NO se ejecuta al arrancar, solo a la hora exacta indicada.
     * Este listener es lo que de verdad dispara una sincronizacion nada mas arrancar la app,
     * en segundo plano (@Async) para no retrasar el arranque.
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        syncAll();
    }

    /** Sincronizacion periodica, cada 24h (ver application.yml para el cron). Recorre todas las listas con dificultad. */
    @Scheduled(cron = "${sync.cron:0 0 4 * * *}")
    public void syncAll() {
        for (GameList gameList : gameListRepository.findAllByHasDifficultyTrue()) {
            syncList(gameList);
        }
    }

    /** Sincroniza una lista concreta (usado también por el botón "Sincronizar todo" del admin). */
    public void syncList(GameList gameList) {
        if (!gameList.isHasDifficulty()) return;

        List<Level> levels = levelRepository.findByGameListOrderByPositionAsc(gameList);
        for (Level level : levels) {
            if (level.getGdId() == null) continue;

            Integer aredlPos = aredlClient.fetchPosition(level.getGdId());
            String gddlDiff = gddlClient.fetchDifficulty(level.getGdId());

            if (aredlPos != null) level.setAredlPosition(aredlPos);
            if (gddlDiff != null) level.setGddlDifficulty(gddlDiff);
            level.setLastSyncedAt(LocalDateTime.now());

            levelRepository.save(level);
        }
    }
}