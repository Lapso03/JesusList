package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.LevelLogEntry;
import com.lapso.gdtracker.model.LevelLogType;
import com.lapso.gdtracker.repository.GameListRepository;
import com.lapso.gdtracker.repository.LevelLogRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

/** Permite a los editores apuntar a mano entradas antiguas del historial (y borrar las que sobren). */
@RestController
@RequestMapping("/admin/api/historial")
public class LevelLogAdminController {

    private final GameListRepository gameListRepository;
    private final LevelLogRepository levelLogRepository;

    public LevelLogAdminController(GameListRepository gameListRepository, LevelLogRepository levelLogRepository) {
        this.gameListRepository = gameListRepository;
        this.levelLogRepository = levelLogRepository;
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
    }

    private ResponseEntity<?> bad(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LogEntryRequest req, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();

        GameList gameList = gameListRepository.findBySlug(req.tipo() == null ? "" : req.tipo()).orElse(null);
        if (gameList == null) return bad("Lista no encontrada");

        LevelLogType type;
        try {
            type = LevelLogType.valueOf(req.type() == null ? "" : req.type());
        } catch (IllegalArgumentException e) {
            return bad("Tipo de cambio no válido");
        }

        String name = req.levelName() == null ? "" : req.levelName().trim();
        if (name.isEmpty()) return bad("Falta el nombre del nivel");

        // Cada tipo de cambio necesita unas posiciones u otras; las que no aplican se descartan.
        Integer oldPos = req.oldPosition();
        Integer newPos = req.newPosition();
        switch (type) {
            case ADDED -> {
                oldPos = null;
                if (newPos == null) return bad("Falta la posición en la que se añadió");
            }
            case REMOVED -> {
                newPos = null;
                if (oldPos == null) return bad("Falta la posición en la que estaba");
            }
            default -> {
                if (oldPos == null || newPos == null) return bad("Faltan la posición anterior y la nueva");
            }
        }
        if ((oldPos != null && oldPos < 1) || (newPos != null && newPos < 1)) {
            return bad("Las posiciones deben ser 1 o mayores");
        }

        LocalDateTime when;
        try {
            when = parseDate(req.date());
        } catch (DateTimeParseException e) {
            return bad("Fecha no válida");
        }

        String actor = (req.actor() == null || req.actor().isBlank()) ? null : req.actor().trim();
        levelLogRepository.save(new LevelLogEntry(gameList, type, name, oldPos, newPos, actor, when));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        if (!SessionUtil.isEditor(session)) return forbidden();
        levelLogRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Vacio = ahora; "yyyy-MM-ddTHH:mm" = fecha y hora; "yyyy-MM-dd" = ese dia a las 12:00. */
    private LocalDateTime parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDateTime.now(LevelLogEntry.ZONE);
        }
        String value = raw.trim();
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(value).atTime(12, 0);
        }
    }
}
