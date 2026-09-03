package com.lapso.gdtracker.service;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.ListType;
import com.lapso.gdtracker.model.Progress;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.LevelCommentRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.repository.ProgressRepository;
import com.lapso.gdtracker.service.sync.AredlClient;
import com.lapso.gdtracker.service.sync.GddlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LevelAdminService {

    private final LevelRepository levelRepository;
    private final AppUserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final AredlClient aredlClient;
    private final GddlClient gddlClient;
    private final LevelCommentRepository levelCommentRepository;

    public LevelAdminService(LevelRepository levelRepository, AppUserRepository userRepository,
                             ProgressRepository progressRepository, AredlClient aredlClient, GddlClient gddlClient, LevelCommentRepository levelCommentRepository) {
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.aredlClient = aredlClient;
        this.gddlClient = gddlClient;
        this.levelCommentRepository = levelCommentRepository;
    }

    @Transactional
    public Level createLevel(ListType listType, int position, String name, Long gdId, String staticDifficulty, String showcaseVideoUrl) {
        List<Level> toShift = levelRepository
                .findByListTypeAndPositionGreaterThanEqualOrderByPositionDesc(listType, position);
        for (Level l : toShift) {
            l.setPosition(l.getPosition() + 1);
            levelRepository.save(l);
        }
        levelRepository.flush();

        Level level = new Level(listType, position, name, gdId, staticDifficulty, showcaseVideoUrl);
        level = levelRepository.save(level);

        for (AppUser user : userRepository.findAll()) {
            progressRepository.save(new Progress(level, user, 0));
        }

        if (gdId != null && listType == ListType.CLASSIC) {
            syncOne(level);
        }

        return level;
    }

    @Transactional
    public void updateDetails(Level level, String name, Long gdId, String staticDifficulty, String showcaseVideoUrl) {
        level.setName(name);
        level.setGdId(gdId);
        level.setStaticDifficulty(staticDifficulty);
        level.setShowcaseVideoUrl(showcaseVideoUrl);
        levelRepository.save(level);
    }

    @Transactional
    public void moveTo(Level level, int newPosition) {
        int oldPosition = level.getPosition();
        if (newPosition == oldPosition) return;

        ListType listType = level.getListType();

        if (newPosition < oldPosition) {
            List<Level> range = levelRepository
                    .findByListTypeAndPositionBetweenOrderByPositionDesc(listType, newPosition, oldPosition - 1);
            for (Level l : range) {
                l.setPosition(l.getPosition() + 1);
                levelRepository.save(l);
            }
        } else {
            List<Level> range = levelRepository
                    .findByListTypeAndPositionBetweenOrderByPositionAsc(listType, oldPosition + 1, newPosition);
            for (Level l : range) {
                l.setPosition(l.getPosition() - 1);
                levelRepository.save(l);
            }
        }
        levelRepository.flush();

        level.setPosition(newPosition);
        levelRepository.save(level);
    }

    @Transactional
    public void deleteLevel(Level level) {
        ListType listType = level.getListType();
        int position = level.getPosition();

        levelCommentRepository.deleteAll(levelCommentRepository.findByLevelOrderByCreatedAtAsc(level));
        progressRepository.deleteAll(progressRepository.findByLevel(level));
        levelRepository.delete(level);
        levelRepository.flush();

        List<Level> toShift = levelRepository
                .findByListTypeAndPositionGreaterThanOrderByPositionAsc(listType, position);
        for (Level l : toShift) {
            l.setPosition(l.getPosition() - 1);
            levelRepository.save(l);
        }
    }

    public int nextPosition(ListType listType) {
        return levelRepository.countByListType(listType) + 1;
    }

    @Transactional
    public void syncOne(Level level) {
        if (level.getGdId() == null) return;

        Integer aredlPos = aredlClient.fetchPosition(level.getGdId());
        String gddlDiff = gddlClient.fetchDifficulty(level.getGdId());

        if (aredlPos != null) level.setAredlPosition(aredlPos);
        if (gddlDiff != null) level.setGddlDifficulty(gddlDiff);
        level.setLastSyncedAt(LocalDateTime.now());

        levelRepository.save(level);
    }
}