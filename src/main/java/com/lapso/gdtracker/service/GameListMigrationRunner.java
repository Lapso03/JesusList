package com.lapso.gdtracker.service;

import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.repository.GameListRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Migración de arranque, de un solo uso por fila. Antes "Classic" y "Platformer" eran un enum
 * fijo (columna "list_type"). Ahora son filas normales de la tabla game_lists y los niveles
 * apuntan a ellas con game_list_id.
 *
 * En un despliegue YA EXISTENTE con datos reales, Hibernate (ddl-auto=update) añade la columna
 * game_list_id vacía sin tocar la columna vieja list_type. Este runner:
 *  1. Crea las filas GameList "classic" y "platformer" si no existen.
 *  2. Para cada nivel con game_list_id NULL, lee su list_type antiguo (vía SQL directo, ya que
 *     el campo ya no existe en la entidad Java) y rellena game_list_id según corresponda.
 *
 * En una base de datos nueva (o ya migrada) la tabla no tendrá columna list_type: el SELECT falla
 * y simplemente no hay nada que migrar, así que se ignora el error.
 *
 * Debe ejecutarse ANTES que DataSeedRunner (@Order más bajo = antes).
 */
@Component
@Order(1)
public class GameListMigrationRunner implements CommandLineRunner {

    private final GameListRepository gameListRepository;
    private final JdbcTemplate jdbcTemplate;

    public GameListMigrationRunner(GameListRepository gameListRepository, JdbcTemplate jdbcTemplate) {
        this.gameListRepository = gameListRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        GameList classic = gameListRepository.findBySlug("classic")
                .orElseGet(() -> gameListRepository.save(
                        new GameList("classic", "Classic", "😈", "Niveles Demon, ranking y progreso", true, 0)));
        GameList platformer = gameListRepository.findBySlug("platformer")
                .orElseGet(() -> gameListRepository.save(
                        new GameList("platformer", "Platformer", "🕹️", "Niveles, completado / no completado", false, 1)));

        migrateLegacyLevels(classic, platformer);
    }

    private void migrateLegacyLevels(GameList classic, GameList platformer) {
        List<Map<String, Object>> rows;
        try {
            rows = jdbcTemplate.queryForList(
                    "SELECT id, list_type FROM levels WHERE game_list_id IS NULL");
        } catch (Exception e) {
            // No existe la columna list_type: base de datos nueva o ya migrada. Nada que hacer.
            return;
        }

        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            String legacyType = (String) row.get("list_type");
            Long targetId = "PLATFORMER".equalsIgnoreCase(legacyType) ? platformer.getId() : classic.getId();
            jdbcTemplate.update("UPDATE levels SET game_list_id = ? WHERE id = ?", targetId, id);
        }

        if (!rows.isEmpty()) {
            System.out.println("[GameListMigrationRunner] Migrados " + rows.size() + " niveles de list_type a game_list_id.");
        }
    }
}