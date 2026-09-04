package com.lapso.gdtracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "levels", uniqueConstraints = @UniqueConstraint(columnNames = {"game_list_id", "position"}))
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * game_list_id se deja SIN "nullable = false" a nivel de columna a propósito: así Hibernate
     * puede añadir la columna con ddl-auto=update sobre una tabla ya poblada (despliegue existente)
     * sin que falle el ALTER TABLE. La migración (GameListMigrationRunner) la rellena al arrancar.
     */
    @ManyToOne
    @JoinColumn(name = "game_list_id", nullable = true)
    private GameList gameList;

    /** Posicion en la lista (1 = mas dificil). Determina los puntos del nivel. */
    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String name;

    /** ID del nivel en Geometry Dash, usado para consultar AREDL/GDDL. */
    @Column(name = "gd_id")
    private Long gdId;

    /** Dificultad estatica importada del Excel original (p.ej. "Extreme Demon (Tier 25)"). */
    @Column(name = "static_difficulty")
    private String staticDifficulty;

    /** Posicion actual en AREDL, obtenida dinamicamente. Null si aun no se ha sincronizado o el nivel no esta en AREDL. */
    @Column(name = "aredl_position")
    private Integer aredlPosition;

    /** Dificultad/tier actual segun GDDL, obtenida dinamicamente. */
    @Column(name = "gddl_difficulty")
    private String gddlDifficulty;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    /** URL de YouTube con el showcase del nivel (se rellena a mano desde el panel de admin). */
    @Column(name = "showcase_video_url")
    private String showcaseVideoUrl;

    protected Level() {
    }

    public Level(GameList gameList, int position, String name, Long gdId, String staticDifficulty, String showcaseVideoUrl) {
        this.gameList = gameList;
        this.position = position;
        this.name = name;
        this.gdId = gdId;
        this.staticDifficulty = staticDifficulty;
        this.showcaseVideoUrl = showcaseVideoUrl;
    }

    /** Puntos que vale el nivel, siguiendo la formula original de la Demonlist: 364.28 * e^(-0.04 * posicion). */
    public double points() {
        return 364.28 * Math.exp(-0.04 * position);
    }

    public Long getId() {
        return id;
    }

    public GameList getGameList() {
        return gameList;
    }

    public void setGameList(GameList gameList) {
        this.gameList = gameList;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGdId() {
        return gdId;
    }

    public void setGdId(Long gdId) {
        this.gdId = gdId;
    }

    public String getStaticDifficulty() {
        return staticDifficulty;
    }

    public void setStaticDifficulty(String staticDifficulty) {
        this.staticDifficulty = staticDifficulty;
    }

    public Integer getAredlPosition() {
        return aredlPosition;
    }

    public void setAredlPosition(Integer aredlPosition) {
        this.aredlPosition = aredlPosition;
    }

    public String getGddlDifficulty() {
        return gddlDifficulty;
    }

    public void setGddlDifficulty(String gddlDifficulty) {
        this.gddlDifficulty = gddlDifficulty;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public String getShowcaseVideoUrl() {return showcaseVideoUrl;
    }

    public void setShowcaseVideoUrl(String showcaseVideoUrl) {this.showcaseVideoUrl = showcaseVideoUrl;
    }
}