package com.lapso.gdtracker.repository;

import com.lapso.gdtracker.model.GameList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameListRepository extends JpaRepository<GameList, Long> {
    Optional<GameList> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<GameList> findAllByOrderByDisplayOrderAsc();
    List<GameList> findAllByHasDifficultyTrue();
}