package com.lapso.gdtracker.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminLevelsController {

    @GetMapping("/admin/niveles")
    public String panel(@RequestParam(required = false, defaultValue = "classic") String tipo,
                        HttpSession session, Model model) {
        if (!SessionUtil.isEditor(session)) {
            return "redirect:/login";
        }
        model.addAttribute("listType", "platformer".equalsIgnoreCase(tipo) ? "platformer" : "classic");
        return "admin-levels";
    }
}