package com.lapso.gdtracker.service;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.ListType;
import com.lapso.gdtracker.model.Progress;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.repository.ProgressRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final AppUserRepository userRepository;
    private final LevelRepository levelRepository;
    private final ProgressRepository progressRepository;

    public ScoreService(AppUserRepository userRepository, LevelRepository levelRepository, ProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.levelRepository = levelRepository;
        this.progressRepository = progressRepository;
    }

    public List<ScoreEntry> globalLeaderboard() {
        List<Level> allLevels = levelRepository.findAll();
        List<AppUser> users = userRepository.findAll();

        return users.stream()
                .map(user -> buildEntry(user, allLevels))
                .sorted(Comparator.comparingDouble(ScoreEntry::getPoints).reversed())
                .toList();
    }

    public List<ScoreEntry> leaderboardFor(ListType listType) {
        List<Level> levels = levelRepository.findByListTypeOrderByPositionAsc(listType);
        List<AppUser> users = userRepository.findAll();

        return users.stream()
                .map(user -> buildEntry(user, levels))
                .sorted(Comparator.comparingDouble(ScoreEntry::getPoints).reversed())
                .toList();
    }

    private ScoreEntry buildEntry(AppUser user, List<Level> levels) {
        Set<Long> levelIds = levels.stream().map(Level::getId).collect(Collectors.toSet());

        List<Progress> progressList = progressRepository.findByUser(user).stream()
                .filter(p -> levelIds.contains(p.getLevel().getId()))
                .toList();

        double points = progressList.stream()
                .filter(Progress::isCompleted)
                .mapToDouble(p -> p.getLevel().points())
                .sum();

        long completed = progressList.stream().filter(Progress::isCompleted).count();

        return new ScoreEntry(user.getUsername(), points, completed);
    }
}