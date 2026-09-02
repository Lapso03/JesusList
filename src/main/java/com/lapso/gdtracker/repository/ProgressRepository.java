package com.lapso.gdtracker.repository;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.Progress;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Optional<Progress> findByLevelAndUser(Level level, AppUser user);
    List<Progress> findByUser(AppUser user);
    List<Progress> findByLevelIn(List<Level> levels);
}
