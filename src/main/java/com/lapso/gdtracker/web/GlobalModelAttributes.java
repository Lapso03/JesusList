package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.repository.GameListRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    private final GameListRepository gameListRepository;

    public GlobalModelAttributes(GameListRepository gameListRepository) {
        this.gameListRepository = gameListRepository;
    }

    @ModelAttribute("currentUser")
    public String currentUser(HttpSession session) {
        return SessionUtil.currentUser(session);
    }

    @ModelAttribute("isEditor")
    public boolean isEditor(HttpSession session) {
        return SessionUtil.isEditor(session);
    }

    /** Disponible en todas las plantillas para pintar la barra de navegación dinámicamente. */
    @ModelAttribute("navLists")
    public List<GameList> navLists() {
        return gameListRepository.findAllByOrderByDisplayOrderAsc();
    }
}