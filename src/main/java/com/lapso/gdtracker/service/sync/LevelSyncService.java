package com.lapso.gdtracker.service.sync;

import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.ListType;
import com.lapso.gdtracker.repository.LevelRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sincroniza la posicion AREDL y la dificultad GDDL de cada nivel de Classic, igual que hacia
 * el script de Google Sheets pero de forma automatica: una vez al arrancar la app (para que los
 * niveles importados del Excel se rellenen tambien) y luego cada 24h via cron.
 */
@Service
public class LevelSyncService {

    private final LevelRepository levelRepository;
    private final AredlClient aredlClient;
    private final GddlClient gddlClient;

    public LevelSyncService(LevelRepository levelRepository, AredlClient aredlClient, GddlClient gddlClient) {
        this.levelRepository = levelRepository;
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
        syncClassicLevels();
    }

    /** Sincronizacion periodica, cada 24h (ver application.yml para el cron). */
    @Scheduled(cron = "${sync.cron:0 0 4 * * *}")
    public void syncClassicLevels() {
        List<Level> levels = levelRepository.findByListTypeOrderByPositionAsc(ListType.CLASSIC);
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