package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.repository.GameListRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.service.LevelAdminService;
import com.lapso.gdtracker.service.sync.LevelSyncService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/niveles")
public class AdminLevelsApiController {

    private final LevelRepository levelRepository;
    private final GameListRepository gameListRepository;
    private final LevelAdminService levelAdminService;
    private final LevelSyncService levelSyncService;

    public AdminLevelsApiController(LevelRepository levelRepository, GameListRepository gameListRepository,
                                    LevelAdminService levelAdminService, LevelSyncService levelSyncService) {
        this.levelRepository = levelRepository;
        this.gameListRepository = gameListRepository;
        this.levelAdminService = levelAdminService;
        this.levelSyncService = levelSyncService;
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
    }

    private GameList resolveList(String slug) {
        return gameListRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada: " + slug));
    }

    private List<LevelDto> listDto(GameList gameList) {
        return levelRepository.findByGameListOrderByPositionAsc(gameList).stream()
                .map(LevelDto::from)
                .toList();
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        return ResponseEntity.ok(listDto(resolveList(tipo)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LevelRequest req, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        GameList gameList = resolveList(req.tipo());
        levelAdminService.createLevel(gameList, req.position(), req.name(), req.gdId(), req.staticDifficulty(),
                req.showcaseVideoUrl());
        return ResponseEntity.ok(listDto(gameList));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody LevelRequest req, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        Level level = levelRepository.findById(id).orElseThrow();
        levelAdminService.updateDetails(level, req.name(), req.gdId(), req.staticDifficulty(), req.showcaseVideoUrl());
        levelAdminService.moveTo(level, req.position());
        return ResponseEntity.ok(listDto(resolveList(req.tipo())));
    }

    @PostMapping("/{id}/sincronizar")
    public ResponseEntity<?> sync(@PathVariable Long id, @RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        Level level = levelRepository.findById(id).orElseThrow();
        levelAdminService.syncOne(level);
        return ResponseEntity.ok(listDto(resolveList(tipo)));
    }

    @PostMapping("/sincronizar-todo")
    public ResponseEntity<?> syncAll(@RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        GameList gameList = resolveList(tipo);
        levelSyncService.syncList(gameList);
        return ResponseEntity.ok(listDto(gameList));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        Level level = levelRepository.findById(id).orElseThrow();
        levelAdminService.deleteLevel(level);
        return ResponseEntity.ok(listDto(resolveList(tipo)));
    }
}