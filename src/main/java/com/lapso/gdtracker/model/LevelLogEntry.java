package com.lapso.gdtracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Una linea del historial de posiciones de una lista. Guarda el nombre del nivel "en foto" (no una
 * relacion a Level) para que el historial siga teniendo sentido aunque el nivel se borre despues.
 */
@Entity
@Table(name = "level_log")
public class LevelLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_list_id", nullable = false)
    private GameList gameList;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private LevelLogType type;

    @Column(name = "level_name", nullable = false)
    private String levelName;

    @Column(name = "old_position")
    private Integer oldPosition;

    @Column(name = "new_position")
    private Integer newPosition;

    /** Usuario que hizo el cambio (el de la sesion). Para ADDED: quien añadio el nivel. */
    private String actor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected LevelLogEntry() {
    }

    public LevelLogEntry(GameList gameList, LevelLogType type, String levelName,
                         Integer oldPosition, Integer newPosition, String actor) {
        this.gameList = gameList;
        this.type = type;
        this.levelName = levelName;
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
        this.actor = actor;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public GameList getGameList() {
        return gameList;
    }

    public LevelLogType getType() {
        return type;
    }

    public String getLevelName() {
        return levelName;
    }

    public Integer getOldPosition() {
        return oldPosition;
    }

    public Integer getNewPosition() {
        return newPosition;
    }

    public String getActor() {
        return actor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
