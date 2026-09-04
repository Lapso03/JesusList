package com.lapso.gdtracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "game_lists")
public class GameList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador usado en las URLs, p.ej. "classic", "platformer", "2-player". Único, sin espacios. */
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String emoji;

    /** Descripción corta que aparece en la tarjeta de la página principal. */
    private String description;

    /**
     * Si es true, los niveles de esta lista tienen ID de GD + dificultad y se sincronizan
     * con AREDL/GDDL (como Classic). Si es false, es una lista simple de completado/no completado
     * (como Platformer).
     */
    @Column(name = "has_difficulty", nullable = false)
    private boolean hasDifficulty;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected GameList() {
    }

    public GameList(String slug, String name, String emoji, String description, boolean hasDifficulty, int displayOrder) {
        this.slug = slug;
        this.name = name;
        this.emoji = emoji;
        this.description = description;
        this.hasDifficulty = hasDifficulty;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHasDifficulty() {
        return hasDifficulty;
    }

    public void setHasDifficulty(boolean hasDifficulty) {
        this.hasDifficulty = hasDifficulty;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}