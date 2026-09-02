package com.lapso.gdtracker.web;

import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AccountController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cuenta")
    public String account(HttpSession session, Model model,
                          @RequestParam(required = false) String error,
                          @RequestParam(required = false) String ok) {
        String username = SessionUtil.currentUser(session);
        if (username == null || SessionUtil.isGuest(username)) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        model.addAttribute("error", error);
        model.addAttribute("ok", ok != null);
        return "account";
    }

    @PostMapping("/cuenta/password")
    public String changePassword(HttpSession session,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword) {
        String username = SessionUtil.currentUser(session);
        if (username == null || SessionUtil.isGuest(username)) {
            return "redirect:/login";
        }

        Optional<AppUser> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            return "redirect:/login";
        }
        AppUser user = maybeUser.get();

        if (user.getPasswordHash() == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return "redirect:/cuenta?error=actual";
        }
        if (newPassword.isBlank() || !newPassword.equals(confirmPassword)) {
            return "redirect:/cuenta?error=nueva";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "redirect:/cuenta?ok=1";
    }
}