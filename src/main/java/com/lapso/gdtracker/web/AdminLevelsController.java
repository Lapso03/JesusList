package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.repository.GameListRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminLevelsController {

    private final GameListRepository gameListRepository;

    public AdminLevelsController(GameListRepository gameListRepository) {
        this.gameListRepository = gameListRepository;
    }

    @GetMapping("/admin/niveles")
    public String panel(@RequestParam(required = false) String tipo,
                        HttpSession session, Model model) {
        if (!SessionUtil.isEditor(session)) {
            return "redirect:/login";
        }

        List<GameList> lists = gameListRepository.findAllByOrderByDisplayOrderAsc();
        String initialSlug = lists.stream()
                .map(GameList::getSlug)
                .filter(slug -> slug.equalsIgnoreCase(tipo))
                .findFirst()
                .orElseGet(() -> lists.isEmpty() ? "" : lists.get(0).getSlug());

        model.addAttribute("lists", lists);
        model.addAttribute("listType", initialSlug);
        return "admin-levels";
    }
}