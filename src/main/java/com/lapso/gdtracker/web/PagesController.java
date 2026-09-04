package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.repository.GameListRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PagesController {

    private final GameListRepository gameListRepository;
    private final LevelRepository levelRepository;

    /**
     * Edita esta lista para añadir/quitar los enlaces que se muestran en /enlaces.
     * title, descripcion corta, url, emoji.
     */
    private static final List<LinkItem> LINKS = List.of(
            new LinkItem("AREDL", "All-Rated Extreme Demon List", "https://aredl.net", "🏆"),
            new LinkItem("GDDL", "Geometry Dash Demon Ladder", "https://gdladder.com", "🪜"),
            new LinkItem("Pointercrate Demonlist", "Lista clásica de demons", "https://pointercrate.com", "👑"),
            new LinkItem("Pemonlist","Lista de demons plataforma","https://pemonlist.com/","🥀"),
            new LinkItem("Tierlist Jesus List", "Tierlist de los niveles de la Jesus List", "https://tiermaker.com/create/jesusgd-tierlist-18605306", "♿"),
            new LinkItem("Higher or Lower AREDL","Juego Higher or Lower para niveles de la AREDL","https://rapidjonte.github.io/AREDL-Higher-or-Lower/","🔝"),
            new LinkItem("GDBrowser", "Perfiles y niveles de GD", "https://gdbrowser.com", "🔍")
    );

    public PagesController(GameListRepository gameListRepository, LevelRepository levelRepository) {
        this.gameListRepository = gameListRepository;
        this.levelRepository = levelRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<GameListSummary> summaries = gameListRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(gl -> new GameListSummary(gl, levelRepository.countByGameList(gl)))
                .toList();
        model.addAttribute("gameLists", summaries);
        return "index";
    }

    @GetMapping("/enlaces")
    public String links(Model model) {
        model.addAttribute("links", LINKS);
        return "links";
    }

    public record GameListSummary(GameList list, int levelCount) {
    }
}