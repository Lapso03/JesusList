package com.lapso.gdtracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Una linea del historial de posiciones de una lista. Guarda el nombre del nivel "en foto" (no una
 * relacion a Level) para que el historial siga teniendo sentido aunque el nivel se borre despues.
 */
@Entity
@Table(name = "level_log")
public class LevelLogEntry {

    /**
     * Las fechas del historial se guardan en hora de España, para que las entradas automaticas
     * y las que se apuntan a mano (que se escriben en hora local) queden en la misma escala.
     */
    public static final ZoneId ZONE = ZoneId.of("Europe/Madrid");

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
        this.createdAt = LocalDateTime.now(ZONE);
    }

    /** Para apuntar a mano un cambio anterior a que existiera el historial, con su fecha real. */
    public LevelLogEntry(GameList gameList, LevelLogType type, String levelName,
                         Integer oldPosition, Integer newPosition, String actor, LocalDateTime createdAt) {
        this(gameList, type, levelName, oldPosition, newPosition, actor);
        this.createdAt = createdAt;
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
