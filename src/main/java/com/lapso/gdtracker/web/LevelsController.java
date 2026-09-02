package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.ListType;
import com.lapso.gdtracker.model.Progress;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.repository.ProgressRepository;
import com.lapso.gdtracker.service.RecommendationService;
import com.lapso.gdtracker.service.ScoreService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class LevelsController {

    private final LevelRepository levelRepository;
    private final AppUserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final ScoreService scoreService;
    private final PasswordEncoder passwordEncoder;
    private final RecommendationService recommendationService;

    public LevelsController(LevelRepository levelRepository, AppUserRepository userRepository,
                            ProgressRepository progressRepository, ScoreService scoreService,
                            PasswordEncoder passwordEncoder, RecommendationService recommendationService) {
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.scoreService = scoreService;
        this.passwordEncoder = passwordEncoder;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false, defaultValue = "classic") String volver,
                             @RequestParam(required = false) String error, Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("volver", volver);
        model.addAttribute("error", error != null);
        return "login";
    }

    /**
     * Login con auto-registro: si el usuario elegido aun no tiene contraseña guardada,
     * la contraseña introducida se guarda como la suya (registro). Si ya tiene una, se compara.
     */
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                         @RequestParam(required = false, defaultValue = "classic") String volver,
                         HttpSession session) {
        Optional<AppUser> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty() || password == null || password.isBlank()) {
            return "redirect:/login?error=1&volver=" + volver;
        }

        AppUser user = maybeUser.get();
        if (user.getPasswordHash() == null) {
            // Primera vez de este usuario: registra la contraseña que acaba de escribir.
            user.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(user);
        } else if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return "redirect:/login?error=1&volver=" + volver;
        }

        session.setAttribute(SessionUtil.SESSION_USER_KEY, username);
        return "redirect:/listas/" + volver;
    }

    /** Entrada como Visitante: sin contraseña, sesión de solo lectura (nunca coincide con un AppUser real). */
    @GetMapping("/login/visitante")
    public String loginAsGuest(HttpSession session,
                                @RequestParam(required = false, defaultValue = "classic") String volver) {
        session.setAttribute(SessionUtil.SESSION_USER_KEY, SessionUtil.GUEST_USERNAME);
        return "redirect:/listas/" + volver;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session,
                          @RequestParam(required = false, defaultValue = "classic") String volver) {
        session.removeAttribute(SessionUtil.SESSION_USER_KEY);
        return "redirect:/listas/" + volver;
    }

    @GetMapping("/listas/{tipo}")
    public String levels(@PathVariable String tipo, HttpSession session, Model model) {
        ListType listType = "platformer".equalsIgnoreCase(tipo) ? ListType.PLATFORMER : ListType.CLASSIC;

        List<Level> levels = levelRepository.findByListTypeOrderByPositionAsc(listType);
        List<AppUser> users = userRepository.findAll();
        List<Progress> allProgress = progressRepository.findByLevelIn(levels);

        Map<Long, Map<String, Integer>> byLevel = new LinkedHashMap<>();
        for (Level level : levels) {
            byLevel.put(level.getId(), new LinkedHashMap<>());
        }
        for (Progress p : allProgress) {
            byLevel.get(p.getLevel().getId()).put(p.getUser().getUsername(), p.getPercentage());
        }

        List<LevelRow> rows = levels.stream()
                .map(level -> new LevelRow(level, byLevel.getOrDefault(level.getId(), Map.of())))
                .toList();

        String currentUser = (String) session.getAttribute(SessionUtil.SESSION_USER_KEY);

        model.addAttribute("rows", rows);
        model.addAttribute("users", users);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("listType", listType.name().toLowerCase());
        model.addAttribute("leaderboard", scoreService.leaderboardFor(listType));
        model.addAttribute("globalLeaderboard", scoreService.globalLeaderboard());

        if (currentUser != null && !SessionUtil.GUEST_USERNAME.equals(currentUser)) {
            userRepository.findByUsername(currentUser).ifPresent(user -> {
                model.addAttribute("recommendations", recommendationService.recommendFor(user, listType));
                if (listType == ListType.CLASSIC) {
                    model.addAttribute("externalRecommendations", recommendationService.recommendExternalDemons(user));
                }
            });
        }
        return "levels";
    }

    @PostMapping("/progreso/actualizar")
    public String updateProgress(@RequestParam Long levelId, @RequestParam int percentage,
                                  @RequestParam String volver, HttpSession session) {
        String username = (String) session.getAttribute(SessionUtil.SESSION_USER_KEY);
        if (username == null || SessionUtil.GUEST_USERNAME.equals(username)) {
            return "redirect:/listas/" + volver;
        }

        Level level = levelRepository.findById(levelId).orElseThrow();
        Optional<AppUser> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            return "redirect:/listas/" + volver;
        }
        AppUser user = maybeUser.get();

        Progress progress = progressRepository.findByLevelAndUser(level, user)
                .orElseGet(() -> new Progress(level, user, 0));
        progress.setPercentage(percentage);
        progressRepository.save(progress);

        return "redirect:/listas/" + volver;
    }
}
