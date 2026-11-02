package com.lapso.gdtracker.repository;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.LevelLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelLogRepository extends JpaRepository<LevelLogEntry, Long> {
    /** Mas recientes primero (por fecha; el id desempata entradas de la misma operacion). */
    List<LevelLogEntry> findByGameListOrderByCreatedAtDescIdDesc(GameList gameList);

    /** Los 10 movimientos mas recientes, para la caja de la pagina de la lista. */
    List<LevelLogEntry> findTop10ByGameListOrderByCreatedAtDescIdDesc(GameList gameList);

    void deleteByGameList(GameList gameList);
}
