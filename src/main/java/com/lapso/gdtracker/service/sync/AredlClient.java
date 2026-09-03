package com.lapso.gdtracker.service.sync;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente del API v2 de AREDL. Equivalente a la funcion getAredl(levelId) del Apps Script original.
 */
@Component
public class AredlClient {

    private final RestClient restClient;
    private final String apiKey;

    public AredlClient(@Value("${sync.aredl.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.aredl.net/v2")
                .build();
    }

    /** Devuelve la posicion actual del nivel en AREDL, o null si no esta en la lista o no se pudo consultar. */
    public Integer fetchPosition(Long gdId) {
        if (gdId == null || apiKey.isBlank()) {
            return null;
        }
        try {
            JsonNode body = restClient.get()
                    .uri("/api/aredl/levels/{id}", gdId)
                    .header("x-api-key", apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null || !body.hasNonNull("position")) {
                return null;
            }
            return body.get("position").asInt();
        } catch (Exception e) {
            // Nivel no encontrado, fuera de lista, o API caida: no rompemos el sync por un nivel.
            e.printStackTrace();
            return null;
        }
    }
}
