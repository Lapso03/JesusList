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
            // GDBrowser no deja filtrar Classic/Platformer directamente en la busqueda, asi que
            // pedimos de sobra y filtramos nosotros por el campo "platformer" de cada nivel.
            int fetchCount = Math.max(count * 6, 30);

            JsonNode body = restClient.get()
                    .uri("/search/*?diff=-2&demonFilter={df}&type=mostliked&count={count}&page=1", demonFilter, fetchCount)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null || !body.isArray()) return results;

            for (JsonNode node : body) {
                if (results.size() >= count) break;
                if (!node.hasNonNull("id") || !node.hasNonNull("name")) continue;

                boolean isPlatformer = node.hasNonNull("platformer") && node.get("platformer").asBoolean();
                if (isPlatformer != wantPlatformer) continue;

                Long id = node.get("id").asLong();
                String name = node.get("name").asText();
                String difficulty = node.hasNonNull("difficulty") ? node.get("difficulty").asText() : null;
                Integer stars = node.hasNonNull("stars") ? node.get("stars").asInt() : null;
                results.add(new GdLevelSuggestion(id, name, difficulty, stars));
            }
        } catch (Exception e) {
            // API publica y sin SLA: si falla, simplemente no hay recomendaciones externas hoy.
        }
        return results;
    }
}