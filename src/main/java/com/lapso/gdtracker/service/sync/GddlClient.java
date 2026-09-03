package com.lapso.gdtracker.service.sync;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente del API de GDDL (gdladder.com). Equivalente a getDemonDifficulty(levelId) del Apps Script original.
 */
@Component
public class GddlClient {

    private final RestClient restClient;
    private final String apiKey;

    public GddlClient(@Value("${sync.gddl.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://gdladder.com/api")
                .build();
    }

    /** Devuelve algo como "Extreme Demon (Tier 24)", o null si no se pudo consultar. */
    public String fetchDifficulty(Long gdId) {
        if (gdId == null || apiKey.isBlank()) {
            return null;
        }
        try {
            JsonNode body = restClient.get()
                    .uri("/level/{id}", gdId)
                    .header("x-api-key", apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null || !body.hasNonNull("Meta") || !body.hasNonNull("Rating")) {
                return null;
            }
            JsonNode meta = body.get("Meta");
            if (!meta.hasNonNull("Difficulty")) {
                return null;
            }
            String difficulty = normalize(meta.get("Difficulty").asText());
            int tier = (int) Math.round(body.get("Rating").asDouble());
            return difficulty + " (Tier " + tier + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String normalize(String diff) {
        String d = diff.toLowerCase();
        if (d.contains("easy")) return "Easy Demon";
        if (d.contains("medium")) return "Medium Demon";
        if (d.contains("hard")) return "Hard Demon";
        if (d.contains("insane")) return "Insane Demon";
        if (d.contains("extreme")) return "Extreme Demon";
        return diff;
    }
}
