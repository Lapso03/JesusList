package com.lapso.gdtracker.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public String currentUser(HttpSession session) {
        return SessionUtil.currentUser(session);
    }

    @ModelAttribute("isEditor")
    public boolean isEditor(HttpSession session) {
        return SessionUtil.isEditor(session);
    }
}