package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.ListType;
import com.lapso.gdtracker.repository.LevelRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PagesController {

    LevelRepository levelRepository;
    public PagesController (LevelRepository levelRepository){
        this.levelRepository = levelRepository;
    }

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

    @GetMapping("/")
    public String home(Model model) {

        // Para futuras listas añadir aquí atributos para contar el número de niveles
        model.addAttribute("classicCount", levelRepository.countByListType(ListType.CLASSIC));
        model.addAttribute("platformerCount", levelRepository.countByListType(ListType.PLATFORMER));
        return "index";
    }

    @GetMapping("/enlaces")
    public String links(Model model) {
        model.addAttribute("links", LINKS);
        return "links";
    }
}