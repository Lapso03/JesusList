package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.GameListRepository;
import com.lapso.gdtracker.repository.LevelLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/** Historial de posiciones de cada lista: altas, movimientos, bajas y pases Main <-> Legacy. */
@Controller
public class LevelLogController {

    private final GameListRepository gameListRepository;
    private final LevelLogRepository levelLogRepository;
    private final AppUserRepository userRepository;

    public LevelLogController(GameListRepository gameListRepository, LevelLogRepository levelLogRepository,
                              AppUserRepository userRepository) {
        this.gameListRepository = gameListRepository;
        this.levelLogRepository = levelLogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/listas/{slug}/log")
    public String log(@PathVariable String slug, Model model) {
        GameList gameList = gameListRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

        model.addAttribute("gameList", gameList);
        model.addAttribute("entries", levelLogRepository.findByGameListOrderByCreatedAtDescIdDesc(gameList));
        // Para el desplegable "usuario" del formulario de entradas manuales (solo lo ven los editores).
        model.addAttribute("users", userRepository.findAll());
        return "list-log";
    }
}
