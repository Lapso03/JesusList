package com.lapso.gdtracker.repository;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findByGameListOrderByPositionAsc(GameList gameList);
    boolean existsByGameList(GameList gameList);

    List<Level> findByGameListAndPositionGreaterThanEqualOrderByPositionDesc(GameList gameList, int position);
    List<Level> findByGameListAndPositionGreaterThanOrderByPositionAsc(GameList gameList, int position);
    List<Level> findByGameListAndPositionBetweenOrderByPositionDesc(GameList gameList, int from, int to);
    List<Level> findByGameListAndPositionBetweenOrderByPositionAsc(GameList gameList, int from, int to);
    int countByGameList(GameList gameList);
}