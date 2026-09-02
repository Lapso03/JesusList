package com.lapso.gdtracker.repository;

import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.ListType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findByListTypeOrderByPositionAsc(ListType listType);
    boolean existsByListType(ListType listType);

    List<Level> findByListTypeAndPositionGreaterThanEqualOrderByPositionDesc(ListType listType, int position);
    List<Level> findByListTypeAndPositionGreaterThanOrderByPositionAsc(ListType listType, int position);
    List<Level> findByListTypeAndPositionBetweenOrderByPositionDesc(ListType listType, int from, int to);
    List<Level> findByListTypeAndPositionBetweenOrderByPositionAsc(ListType listType, int from, int to);
    int countByListType(ListType listType);
}