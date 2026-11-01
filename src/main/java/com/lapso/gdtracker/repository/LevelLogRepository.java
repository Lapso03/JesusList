package com.lapso.gdtracker.repository;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.LevelLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelLogRepository extends JpaRepository<LevelLogEntry, Long> {
    /** Mas recientes primero. */
    List<LevelLogEntry> findByGameListOrderByIdDesc(GameList gameList);

    void deleteByGameList(GameList gameList);
}
