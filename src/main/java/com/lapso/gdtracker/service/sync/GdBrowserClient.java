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

    /** @param demonFilter 1=Easy, 2=Medium, 3=Hard, 4=Insane, 5=Extreme */
    public List<GdLevelSuggestion> searchDemons(int demonFilter, int count) {
        List<GdLevelSuggestion> results = new ArrayList<>();
        try {
            JsonNode body = restClient.get()
                    .uri("/search/*?diff=-2&demonFilter={df}&type=mostliked&count={count}&page=1", demonFilter, count)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null || !body.isArray()) return results;

            for (JsonNode node : body) {
                if (!node.hasNonNull("id") || !node.hasNonNull("name")) continue;
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