package com.lapso.gdtracker.service.sync;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class GdBrowserClient {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://gdbrowser.com/api")
            .build();

    /**
     * @param demonFilter 1=Easy, 2=Medium, 3=Hard, 4=Insane, 5=Extreme
     * @param wantPlatformer true = solo niveles en modo Platformer, false = solo niveles en modo Classic
     * @param count cuantos resultados finales se quieren (ya filtrados por modo)
     */
    public List<GdLevelSuggestion> searchDemons(int demonFilter, boolean wantPlatformer, int count) {
        List<GdLevelSuggestion> results = new ArrayList<>();
        try {
            // El endpoint de busqueda no siempre trae el campo "platformer" correcto (es un campo
            // ligero pensado para listados), asi que solo lo usamos para obtener candidatos por
            // popularidad, y luego confirmamos el modo real de cada uno con /api/level/{id},
            // que si es fiable. Nos detenemos en cuanto tenemos "count" confirmados.
            int fetchCount = Math.max(count * 8, 40);

            JsonNode searchBody = restClient.get()
                    .uri("/search/*?diff=-2&demonFilter={df}&type=mostliked&count={count}&page=1", demonFilter, fetchCount)
                    .retrieve()
                    .body(JsonNode.class);

            if (searchBody == null || !searchBody.isArray()) return results;

            int checked = 0;
            int maxChecks = fetchCount; // limite de seguridad para no martillear la API indefinidamente

            for (JsonNode candidate : searchBody) {
                if (results.size() >= count || checked >= maxChecks) break;
                if (!candidate.hasNonNull("id") || !candidate.hasNonNull("name")) continue;

                checked++;
                Long id = candidate.get("id").asLong();

                JsonNode detail;
                try {
                    detail = restClient.get()
                            .uri("/level/{id}", id)
                            .retrieve()
                            .body(JsonNode.class);
                } catch (Exception e) {
                    continue; // este nivel en concreto no se pudo verificar, pasamos al siguiente
                }
                if (detail == null) continue;

                boolean isPlatformer = detail.hasNonNull("platformer") && detail.get("platformer").asBoolean();
                if (isPlatformer != wantPlatformer) continue;

                String name = detail.hasNonNull("name") ? detail.get("name").asText() : candidate.get("name").asText();
                String difficulty = detail.hasNonNull("difficulty") ? detail.get("difficulty").asText() : null;
                Integer stars = detail.hasNonNull("stars") ? detail.get("stars").asInt() : null;
                results.add(new GdLevelSuggestion(id, name, difficulty, stars));
            }
        } catch (Exception e) {
            // API publica y sin SLA: si falla, simplemente no hay recomendaciones externas hoy.
        }
        return results;
    }
}