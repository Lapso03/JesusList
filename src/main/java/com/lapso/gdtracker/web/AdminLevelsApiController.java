package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.ListType;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.service.LevelAdminService;
import com.lapso.gdtracker.service.sync.LevelSyncService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/niveles")
public class AdminLevelsApiController {

    private final LevelRepository levelRepository;
    private final LevelAdminService levelAdminService;
    private final LevelSyncService levelSyncService;

    public AdminLevelsApiController(LevelRepository levelRepository, LevelAdminService levelAdminService,
                                    LevelSyncService levelSyncService) {
        this.levelRepository = levelRepository;
        this.levelAdminService = levelAdminService;
        this.levelSyncService = levelSyncService;
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
    }

    private ListType parseType(String tipo) {
        return "platformer".equalsIgnoreCase(tipo) ? ListType.PLATFORMER : ListType.CLASSIC;
    }

    private List<LevelDto> listDto(ListType listType) {
        return levelRepository.findByListTypeOrderByPositionAsc(listType).stream()
                .map(LevelDto::from)
                .toList();
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        return ResponseEntity.ok(listDto(parseType(tipo)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LevelRequest req, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        ListType listType = parseType(req.tipo());
        levelAdminService.createLevel(listType, req.position(), req.name(), req.gdId(), req.staticDifficulty(),
                req.showcaseVideoUrl());
        return ResponseEntity.ok(listDto(listType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody LevelRequest req, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        Level level = levelRepository.findById(id).orElseThrow();
        levelAdminService.updateDetails(level, req.name(), req.gdId(), req.staticDifficulty(), req.showcaseVideoUrl());
        levelAdminService.moveTo(level, req.position());
        return ResponseEntity.ok(listDto(parseType(req.tipo())));
    }

    @PostMapping("/{id}/sincronizar")
    public ResponseEntity<?> sync(@PathVariable Long id, @RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        Level level = levelRepository.findById(id).orElseThrow();
        levelAdminService.syncOne(level);
        return ResponseEntity.ok(listDto(parseType(tipo)));
    }

    @PostMapping("/sincronizar-todo")
    public ResponseEntity<?> syncAll(@RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        levelSyncService.syncClassicLevels();
        return ResponseEntity.ok(listDto(parseType(tipo)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam String tipo, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        Level level = levelRepository.findById(id).orElseThrow();
        levelAdminService.deleteLevel(level);
        return ResponseEntity.ok(listDto(parseType(tipo)));
    }
}