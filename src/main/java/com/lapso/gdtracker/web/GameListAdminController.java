package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.repository.GameListRepository;
import com.lapso.gdtracker.service.GameListAdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/listas")
public class GameListAdminController {

    private final GameListRepository gameListRepository;
    private final GameListAdminService gameListAdminService;

    public GameListAdminController(GameListRepository gameListRepository, GameListAdminService gameListAdminService) {
        this.gameListRepository = gameListRepository;
        this.gameListAdminService = gameListAdminService;
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
    }

    private List<GameListDto> allDto() {
        return gameListAdminService.findAll().stream().map(GameListDto::from).toList();
    }

    @GetMapping
    public ResponseEntity<?> list(HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        return ResponseEntity.ok(allDto());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GameListRequest req, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        if (req.name() == null || req.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio"));
        }
        gameListAdminService.create(req.name(), req.emoji(), req.description(), req.hasDifficulty());
        return ResponseEntity.ok(allDto());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GameListRequest req, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        GameList gameList = gameListRepository.findById(id).orElseThrow();
        gameListAdminService.update(gameList, req.name(), req.emoji(), req.description(), req.hasDifficulty());
        return ResponseEntity.ok(allDto());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        GameList gameList = gameListRepository.findById(id).orElseThrow();
        gameListAdminService.delete(gameList);
        return ResponseEntity.ok(allDto());
    }
}