package com.lapso.gdtracker.service.sync;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente del API de GDDL (gdladder.com). Equivalente a getDemonDifficulty(levelId) del Apps Script original.
 *
 * La ruta correcta es /api/levels/{id}, en PLURAL. La antigua /api/level/{id} (singular) ya no
 * existe y GDDL contestaba "Cannot GET /api/level/...". Consultar un nivel es publico: la clave
 * (GDDL_API_KEY) solo se usa como respaldo si el servidor responde 401/403 sin ella.
 */
@Component
public class GddlClient {

    private static final Logger log = LoggerFactory.getLogger(GddlClient.class);
    private static final String USER_AGENT = "JesusList/1.0 (+https://github.com/Lapso03/JesusList)";

    private final RestClient restClient;
    private final String apiKey;

    public GddlClient(@Value("${sync.gddl.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://gdladder.com/api")
                .build();
    }

    /** Devuelve algo como "Extreme Demon (Tier 24)", o null si el nivel no esta en GDDL o no se pudo consultar. */
    public String fetchDifficulty(Long gdId) {
        if (gdId == null) {
            return null;
        }
        try {
            JsonNode body;
            try {
                body = request(gdId, false);
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                if ((status == 401 || status == 403) && !apiKey.isBlank()) {
                    body = request(gdId, true);
                } else {
                    throw e;
                }
            }
            if (body == null) {
                return null;
            }

            // El tier va en la raiz del JSON: "Rating" (media de votos, puede ser null) o, si no hay
            // votos todavia, "DefaultRating".
            Double tier = number(body, "Rating");
            if (tier == null) {
                tier = number(body, "DefaultRating");
            }
            if (tier == null) {
                return null;
            }
            return GddlTiers.describe(tier);
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 404) {
                log.debug("GDDL: el nivel {} no esta en GDDL", gdId);
            } else {
                log.warn("GDDL: respuesta {} al consultar el nivel {}", status, gdId);
            }
            return null;
        } catch (Exception e) {
            log.warn("GDDL: error consultando el nivel {}: {}", gdId, e.toString());
            return null;
        }
    }

    private JsonNode request(Long gdId, boolean withKey) {
        if (withKey) {
            return restClient.get()
                    .uri("/levels/{id}", gdId)
                    .header("User-Agent", USER_AGENT)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(JsonNode.class);
        }
        return restClient.get()
                .uri("/levels/{id}", gdId)
                .header("User-Agent", USER_AGENT)
                .retrieve()
                .body(JsonNode.class);
    }

    private static Double number(JsonNode body, String field) {
        if (body.hasNonNull(field) && body.get(field).isNumber()) {
            return body.get(field).asDouble();
        }
        return null;
    }
}
