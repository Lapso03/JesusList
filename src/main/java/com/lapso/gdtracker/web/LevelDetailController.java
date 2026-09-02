package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.LevelComment;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.LevelCommentRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LevelDetailController {

    private final LevelRepository levelRepository;
    private final LevelCommentRepository commentRepository;
    private final AppUserRepository userRepository;

    public LevelDetailController(LevelRepository levelRepository, LevelCommentRepository commentRepository,
                                 AppUserRepository userRepository) {
        this.levelRepository = levelRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/niveles/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Level level = levelRepository.findById(id).orElseThrow();

        model.addAttribute("level", level);
        model.addAttribute("comments", commentRepository.findByLevelOrderByCreatedAtAsc(level));
        model.addAttribute("embedUrl", toEmbedUrl(level.getShowcaseVideoUrl()));

        String searchQuery = level.getName() + " geometry dash showcase";
        model.addAttribute("youtubeSearchUrl",
                "https://www.youtube.com/results?search_query=" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8));

        return "level-detail";
    }

    /**
     * Convierte un enlace normal de YouTube (watch?v=..., youtu.be/..., shorts/...) al formato
     * /embed/ necesario para incrustarlo en un iframe. Si ya viene en formato /embed/, no lo toca.
     */
    private String toEmbedUrl(String url) {
        if (url == null || url.isBlank()) return null;
        if (url.contains("youtube.com/embed/")) return url;

        String videoId = null;
        try {
            if (url.contains("youtu.be/")) {
                videoId = url.substring(url.indexOf("youtu.be/") + 9);
            } else if (url.contains("watch?v=")) {
                videoId = url.substring(url.indexOf("watch?v=") + 8);
            } else if (url.contains("/shorts/")) {
                videoId = url.substring(url.indexOf("/shorts/") + 8);
            }
            if (videoId != null) {
                int ampIndex = videoId.indexOf('&');
                if (ampIndex != -1) videoId = videoId.substring(0, ampIndex);
                int qIndex = videoId.indexOf('?');
                if (qIndex != -1) videoId = videoId.substring(0, qIndex);
                return "https://www.youtube.com/embed/" + videoId;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @PostMapping("/niveles/{id}/comentarios")
    public String addComment(@PathVariable Long id, @RequestParam String text, HttpSession session) {
        String username = SessionUtil.currentUser(session);
        if (username == null || SessionUtil.isGuest(username) || text == null || text.isBlank()) {
            return "redirect:/niveles/" + id;
        }

        AppUser user = userRepository.findByUsername(username).orElseThrow();
        Level level = levelRepository.findById(id).orElseThrow();
        commentRepository.save(new LevelComment(level, user, text.trim()));

        return "redirect:/niveles/" + id;
    }
}