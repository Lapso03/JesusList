package com.lapso.gdtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lapso.gdtracker.model.AppUser;
import com.lapso.gdtracker.model.GameList;
import com.lapso.gdtracker.model.Level;
import com.lapso.gdtracker.model.Progress;
import com.lapso.gdtracker.repository.AppUserRepository;
import com.lapso.gdtracker.repository.GameListRepository;
import com.lapso.gdtracker.repository.LevelRepository;
import com.lapso.gdtracker.repository.ProgressRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Al arrancar la aplicacion por primera vez (base de datos vacia), importa los niveles
 * de Classic y los de Platformer junto con el progreso original de GdLali, Bimba666 y Lapso,
 * extraidos del Excel "Jesus Demonlist", y crea los 3 usuarios (sin contraseña: cada uno la fija
 * la primera vez que inicia sesión, ver LevelsController).
 *
 * Se ejecuta DESPUES de GameListMigrationRunner (@Order 2 > 1), que es quien garantiza que las
 * listas "classic" y "platformer" ya existen como GameList antes de que esto se ejecute.
 */
@Component
@Order(2)
public class DataSeedRunner implements CommandLineRunner {

    private static final String[] USERNAMES = {"GdLali", "Bimba666", "Lapso"};

    private final AppUserRepository userRepository;
    private final LevelRepository levelRepository;
    private final ProgressRepository progressRepository;
    private final GameListRepository gameListRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataSeedRunner(AppUserRepository userRepository, LevelRepository levelRepository,
                          ProgressRepository progressRepository, GameListRepository gameListRepository) {
        this.userRepository = userRepository;
        this.levelRepository = levelRepository;
        this.progressRepository = progressRepository;
        this.gameListRepository = gameListRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (String username : USERNAMES) {
            userRepository.findByUsername(username).orElseGet(() -> userRepository.save(new AppUser(username)));
        }

        GameList classic = gameListRepository.findBySlug("classic").orElseThrow();
        GameList platformer = gameListRepository.findBySlug("platformer").orElseThrow();

        if (!levelRepository.existsByGameList(classic)) {
            importList("seed/classic.json", classic, true);
        }
        if (!levelRepository.existsByGameList(platformer)) {
            importList("seed/platformer.json", platformer, false);
        }
    }

    private void importList(String resourcePath, GameList gameList, boolean hasDifficulty) throws Exception {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            for (JsonNode node : root) {
                int position = node.get("position").asInt();
                String name = node.get("name").asText();
                Long gdId = hasDifficulty && node.hasNonNull("gdId") ? node.get("gdId").asLong() : null;
                String difficulty = hasDifficulty && node.hasNonNull("difficulty") ? node.get("difficulty").asText() : null;

                // No estoy seguro de que esto esté bien
                String showcaseVideoUrl = hasDifficulty && node.hasNonNull("showcaseVideoUrl") ? node.get("difficulty").asText() : null;

                Level level = levelRepository.save(new Level(gameList, position, name, gdId, difficulty, showcaseVideoUrl));

                JsonNode progressNode = node.get("progress");
                Iterator<String> usernames = progressNode.fieldNames();
                while (usernames.hasNext()) {
                    String username = usernames.next();
                    int pct = progressNode.get(username).asInt();
                    AppUser user = userRepository.findByUsername(username).orElseThrow();
                    progressRepository.save(new Progress(level, user, pct));
                }
            }
        }
    }
}