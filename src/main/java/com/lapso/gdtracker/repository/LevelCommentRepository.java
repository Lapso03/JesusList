package com.lapso.gdtracker.repository;

import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.LevelComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelCommentRepository extends JpaRepository<LevelComment, Long> {
    List<LevelComment> findByLevelOrderByCreatedAtAsc(Level level);
}