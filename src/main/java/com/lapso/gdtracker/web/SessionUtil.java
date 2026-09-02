package com.lapso.gdtracker.web;

import jakarta.servlet.http.HttpSession;

import java.util.Set;

public final class SessionUtil {

    public static final String SESSION_USER_KEY = "currentUsername";
    public static final String GUEST_USERNAME = "Visitante";

    /** Los 3 unicos usuarios que pueden editar progreso y administrar niveles. */
    public static final Set<String> EDITOR_USERNAMES = Set.of("GdLali", "Bimba666", "Lapso");

    private SessionUtil() {
    }

    public static String currentUser(HttpSession session) {
        return (String) session.getAttribute(SESSION_USER_KEY);
    }

    public static boolean isGuest(String username) {
        return username == null || GUEST_USERNAME.equals(username);
    }

    public static boolean isEditor(HttpSession session) {
        String username = currentUser(session);
        return username != null && EDITOR_USERNAMES.contains(username);
    }
}